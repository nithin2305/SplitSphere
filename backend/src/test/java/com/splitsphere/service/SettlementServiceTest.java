package com.splitsphere.service;

import com.splitsphere.dto.SettlementRequest;
import com.splitsphere.dto.SettlementResponse;
import com.splitsphere.model.Group;
import com.splitsphere.model.Settlement;
import com.splitsphere.model.User;
import com.splitsphere.repository.GroupRepository;
import com.splitsphere.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {
    
    @Mock
    private SettlementRepository settlementRepository;
    
    @Mock
    private GroupRepository groupRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private BalanceService balanceService;
    
    @InjectMocks
    private SettlementService settlementService;
    
    private User payer;
    private User payee;
    private Group group;
    private SettlementRequest settlementRequest;
    
    @BeforeEach
    void setUp() {
        payer = new User();
        payer.setId(1L);
        payer.setUserId("user1");
        payer.setAccountName("User One");
        
        payee = new User();
        payee.setId(2L);
        payee.setUserId("user2");
        payee.setAccountName("User Two");
        
        group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setClosed(false);
        
        Set<User> members = new HashSet<>();
        members.add(payer);
        members.add(payee);
        group.setMembers(members);
        
        settlementRequest = new SettlementRequest();
        settlementRequest.setGroupId(1L);
        settlementRequest.setPayeeUserId("user2");
        settlementRequest.setAmount(new BigDecimal("50.00"));
        settlementRequest.setNote("Test settlement");
    }
    
    @Test
    void testCreateSettlement_Success() {
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(balanceService.calculateBalanceBetweenUsers(1L, payer, payee))
                .thenReturn(new BigDecimal("100.00"));
        
        Settlement settlement = new Settlement();
        settlement.setId(1L);
        settlement.setPayer(payer);
        settlement.setPayee(payee);
        settlement.setGroup(group);
        settlement.setAmount(new BigDecimal("50.00"));
        settlement.setNote("Test settlement");
        
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);
        
        SettlementResponse response = settlementService.createSettlement(settlementRequest, "user1");
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user1", response.getPayerUserId());
        assertEquals("user2", response.getPayeeUserId());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        
        verify(settlementRepository).save(any(Settlement.class));
        verify(auditService).log(eq("CREATE"), eq("Settlement"), any(), any(), anyString());
    }
    
    @Test
    void testCreateSettlement_ExceedsBalance() {
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(balanceService.calculateBalanceBetweenUsers(1L, payer, payee))
                .thenReturn(new BigDecimal("30.00"));
        
        settlementRequest.setAmount(new BigDecimal("50.00"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
    
    @Test
    void testCreateSettlement_NoDebt() {
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(balanceService.calculateBalanceBetweenUsers(1L, payer, payee))
                .thenReturn(BigDecimal.ZERO);
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
    
    @Test
    void testCreateSettlement_PayeeOwesPayerInstead() {
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        // Negative balance means payee owes payer, not the other way around
        when(balanceService.calculateBalanceBetweenUsers(1L, payer, payee))
                .thenReturn(new BigDecimal("-50.00"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
    
    @Test
    void testCreateSettlement_ClosedGroup() {
        group.setClosed(true);
        
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
    
    @Test
    void testCreateSettlement_PayerNotMember() {
        Set<User> members = new HashSet<>();
        members.add(payee);
        group.setMembers(members);
        
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user2")).thenReturn(payee);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
    
    @Test
    void testCreateSettlement_SamePayerAndPayee() {
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(userService.getUserByUserId("user1")).thenReturn(payer);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        settlementRequest.setPayeeUserId("user1");
        
        assertThrows(IllegalArgumentException.class, () -> {
            settlementService.createSettlement(settlementRequest, "user1");
        });
        
        verify(settlementRepository, never()).save(any(Settlement.class));
    }
}
