package com.splitsphere.service;

import com.splitsphere.dto.SettlementRequest;
import com.splitsphere.dto.SettlementResponse;
import com.splitsphere.model.Group;
import com.splitsphere.model.Settlement;
import com.splitsphere.model.User;
import com.splitsphere.repository.GroupRepository;
import com.splitsphere.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final AuditService auditService;
    
    @Transactional
    public SettlementResponse createSettlement(SettlementRequest request, String payerUserId) {
        User payer = userService.getUserByUserId(payerUserId);
        User payee = userService.getUserByUserId(request.getPayeeUserId());
        
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        if (group.isClosed()) {
            throw new IllegalArgumentException("Cannot add settlement to a closed group");
        }
        
        if (!group.getMembers().contains(payer)) {
            throw new IllegalArgumentException("Payer is not a member of this group");
        }
        
        if (!group.getMembers().contains(payee)) {
            throw new IllegalArgumentException("Payee is not a member of this group");
        }
        
        if (payer.getId().equals(payee.getId())) {
            throw new IllegalArgumentException("Cannot settle payment with yourself");
        }
        
        Settlement settlement = new Settlement();
        settlement.setPayer(payer);
        settlement.setPayee(payee);
        settlement.setGroup(group);
        settlement.setAmount(request.getAmount());
        settlement.setNote(request.getNote());
        
        settlement = settlementRepository.save(settlement);
        
        auditService.log("CREATE", "Settlement", settlement.getId(), payer,
                "Settlement created: " + payer.getAccountName() + " paid " + payee.getAccountName() + " - " + settlement.getAmount());
        
        return toSettlementResponse(settlement);
    }
    
    @Transactional(readOnly = true)
    public List<SettlementResponse> getGroupSettlements(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        return settlementRepository.findByGroupOrderByCreatedAtDesc(group).stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());
    }
    
    private SettlementResponse toSettlementResponse(Settlement settlement) {
        SettlementResponse response = new SettlementResponse();
        response.setId(settlement.getId());
        response.setPayerUserId(settlement.getPayer().getUserId());
        response.setPayerName(settlement.getPayer().getAccountName());
        response.setPayeeUserId(settlement.getPayee().getUserId());
        response.setPayeeName(settlement.getPayee().getAccountName());
        response.setAmount(settlement.getAmount());
        response.setNote(settlement.getNote());
        response.setCreatedAt(settlement.getCreatedAt());
        
        return response;
    }
}
