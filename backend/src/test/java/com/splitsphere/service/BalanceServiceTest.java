package com.splitsphere.service;

import com.splitsphere.dto.BalanceResponse;
import com.splitsphere.model.Expense;
import com.splitsphere.model.Group;
import com.splitsphere.model.Settlement;
import com.splitsphere.model.User;
import com.splitsphere.repository.ExpenseRepository;
import com.splitsphere.repository.GroupRepository;
import com.splitsphere.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private GroupRepository groupRepository;
    
    @Mock
    private SettlementRepository settlementRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private BalanceService balanceService;
    
    private User user1;
    private User user2;
    private User user3;
    private Group group;
    
    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUserId("user1");
        user1.setAccountName("User One");
        
        user2 = new User();
        user2.setId(2L);
        user2.setUserId("user2");
        user2.setAccountName("User Two");
        
        user3 = new User();
        user3.setId(3L);
        user3.setUserId("user3");
        user3.setAccountName("User Three");
        
        group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        
        Set<User> members = new HashSet<>();
        members.add(user1);
        members.add(user2);
        members.add(user3);
        group.setMembers(members);
    }
    
    @Test
    void testGetGroupBalances_WithExpensesOnly() {
        when(userService.getUserByUserId("user1")).thenReturn(user1);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User2 paid 300, split among user1, user2, user3 (100 each)
        // So user1 owes user2 100, and user3 owes user2 100
        Expense expense = new Expense();
        expense.setPayer(user2);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group)).thenReturn(Collections.emptyList());
        
        List<BalanceResponse> balances = balanceService.getGroupBalances(1L, "user1");
        
        assertEquals(1, balances.size());
        assertEquals("user2", balances.get(0).getUserId());
        assertEquals(new BigDecimal("100.00"), balances.get(0).getBalance());
        assertEquals("owes", balances.get(0).getStatus());
    }
    
    @Test
    void testGetGroupBalances_WithExpensesAndSettlements() {
        when(userService.getUserByUserId("user1")).thenReturn(user1);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User2 paid 300, split among user1, user2, user3 (100 each)
        Expense expense = new Expense();
        expense.setPayer(user2);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        
        // User1 settled 60 with user2
        Settlement settlement = new Settlement();
        settlement.setPayer(user1);
        settlement.setPayee(user2);
        settlement.setAmount(new BigDecimal("60.00"));
        settlement.setGroup(group);
        
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group))
                .thenReturn(Collections.singletonList(settlement));
        
        List<BalanceResponse> balances = balanceService.getGroupBalances(1L, "user1");
        
        // User1 owed 100, paid 60, so now owes 40
        assertEquals(1, balances.size());
        assertEquals("user2", balances.get(0).getUserId());
        assertEquals(new BigDecimal("40.00"), balances.get(0).getBalance());
        assertEquals("owes", balances.get(0).getStatus());
    }
    
    @Test
    void testGetGroupBalances_FullySettled() {
        when(userService.getUserByUserId("user1")).thenReturn(user1);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User2 paid 300, split among user1, user2, user3 (100 each)
        Expense expense = new Expense();
        expense.setPayer(user2);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        
        // User1 settled full 100 with user2
        Settlement settlement = new Settlement();
        settlement.setPayer(user1);
        settlement.setPayee(user2);
        settlement.setAmount(new BigDecimal("100.00"));
        settlement.setGroup(group);
        
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group))
                .thenReturn(Collections.singletonList(settlement));
        
        List<BalanceResponse> balances = balanceService.getGroupBalances(1L, "user1");
        
        // User1 owed 100, paid 100, so now owes 0 (shouldn't appear in results)
        assertEquals(0, balances.size());
    }
    
    @Test
    void testCalculateBalanceBetweenUsers_WithExpenses() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User2 paid 300, split among user1, user2, user3 (100 each)
        Expense expense = new Expense();
        expense.setPayer(user2);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group)).thenReturn(Collections.emptyList());
        
        BigDecimal balance = balanceService.calculateBalanceBetweenUsers(1L, user1, user2);
        
        // User1 owes user2 100
        assertEquals(new BigDecimal("100.00"), balance);
    }
    
    @Test
    void testCalculateBalanceBetweenUsers_WithExpensesAndSettlements() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User2 paid 300, split among user1, user2, user3 (100 each)
        Expense expense = new Expense();
        expense.setPayer(user2);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        
        // User1 settled 60 with user2
        Settlement settlement = new Settlement();
        settlement.setPayer(user1);
        settlement.setPayee(user2);
        settlement.setAmount(new BigDecimal("60.00"));
        
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group))
                .thenReturn(Collections.singletonList(settlement));
        
        BigDecimal balance = balanceService.calculateBalanceBetweenUsers(1L, user1, user2);
        
        // User1 owed 100, paid 60, so owes 40
        assertEquals(new BigDecimal("40.00"), balance);
    }
    
    @Test
    void testCalculateBalanceBetweenUsers_NegativeBalance() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        
        // User1 paid 300, split among user1, user2, user3 (100 each)
        Expense expense = new Expense();
        expense.setPayer(user1);
        expense.setAmount(new BigDecimal("300.00"));
        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        expense.setParticipants(participants);
        
        when(expenseRepository.findByGroup(group)).thenReturn(Collections.singletonList(expense));
        when(settlementRepository.findByGroupOrderByCreatedAtDesc(group)).thenReturn(Collections.emptyList());
        
        BigDecimal balance = balanceService.calculateBalanceBetweenUsers(1L, user1, user2);
        
        // User2 owes user1 100, so from user1's perspective it's negative
        assertEquals(new BigDecimal("-100.00"), balance);
    }
}
