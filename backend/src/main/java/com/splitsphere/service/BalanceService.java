package com.splitsphere.service;

import com.splitsphere.dto.BalanceResponse;
import com.splitsphere.model.Expense;
import com.splitsphere.model.Group;
import com.splitsphere.model.User;
import com.splitsphere.repository.ExpenseRepository;
import com.splitsphere.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceService {
    
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserService userService;
    
    @Transactional(readOnly = true)
    public List<BalanceResponse> getGroupBalances(Long groupId, String userId) {
        User currentUser = userService.getUserByUserId(userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        if (!group.getMembers().contains(currentUser)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }
        
        List<Expense> expenses = expenseRepository.findByGroup(group);
        
        // Calculate balances for each user relative to current user
        Map<User, BigDecimal> balances = new HashMap<>();
        
        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            Set<User> participants = expense.getParticipants();
            
            if (participants.isEmpty()) {
                continue;
            }
            
            BigDecimal perPersonAmount = expense.getAmount()
                    .divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);
            
            for (User participant : participants) {
                if (!participant.equals(payer)) {
                    // Participant owes payer
                    if (participant.equals(currentUser)) {
                        // Current user owes payer
                        balances.merge(payer, perPersonAmount.negate(), BigDecimal::add);
                    } else if (payer.equals(currentUser)) {
                        // Participant owes current user
                        balances.merge(participant, perPersonAmount, BigDecimal::add);
                    }
                }
            }
        }
        
        List<BalanceResponse> responses = new ArrayList<>();
        for (Map.Entry<User, BigDecimal> entry : balances.entrySet()) {
            User user = entry.getKey();
            BigDecimal balance = entry.getValue();
            
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                BalanceResponse response = new BalanceResponse();
                response.setUserId(user.getUserId());
                response.setUserName(user.getAccountName());
                response.setBalance(balance.abs());
                response.setStatus(balance.compareTo(BigDecimal.ZERO) > 0 ? "owed" : "owes");
                responses.add(response);
            }
        }
        
        return responses;
    }
}
