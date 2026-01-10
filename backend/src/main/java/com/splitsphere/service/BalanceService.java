package com.splitsphere.service;

import com.splitsphere.dto.BalanceResponse;
import com.splitsphere.model.Expense;
import com.splitsphere.model.Group;
import com.splitsphere.model.Settlement;
import com.splitsphere.model.User;
import com.splitsphere.repository.ExpenseRepository;
import com.splitsphere.repository.GroupRepository;
import com.splitsphere.repository.SettlementRepository;
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
    private final SettlementRepository settlementRepository;
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
        List<Settlement> settlements = settlementRepository.findByGroupOrderByCreatedAtDesc(group);
        
        // Calculate balances for each user relative to current user
        Map<User, BigDecimal> balances = new HashMap<>();
        
        // Process expenses
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
        
        // Process settlements
        for (Settlement settlement : settlements) {
            User payer = settlement.getPayer();
            User payee = settlement.getPayee();
            BigDecimal amount = settlement.getAmount();
            
            // Settlement means payer paid payee, so payer's debt to payee is reduced
            if (payer.equals(currentUser)) {
                // Current user paid someone, so that person owes current user less
                balances.merge(payee, amount.negate(), BigDecimal::add);
            } else if (payee.equals(currentUser)) {
                // Someone paid current user, so current user owes them less
                balances.merge(payer, amount, BigDecimal::add);
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
    
    /**
     * Calculate how much payer owes to payee in a specific group.
     * Positive value means payer owes payee.
     * Negative value means payee owes payer.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateBalanceBetweenUsers(Long groupId, User payer, User payee) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        List<Expense> expenses = expenseRepository.findByGroup(group);
        List<Settlement> settlements = settlementRepository.findByGroupOrderByCreatedAtDesc(group);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        // Process expenses
        for (Expense expense : expenses) {
            User expensePayer = expense.getPayer();
            Set<User> participants = expense.getParticipants();
            
            if (participants.isEmpty()) {
                continue;
            }
            
            BigDecimal perPersonAmount = expense.getAmount()
                    .divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);
            
            // If payee paid and payer participated (not as payer), payer owes payee
            if (expensePayer.equals(payee) && participants.contains(payer) && !expensePayer.equals(payer)) {
                balance = balance.add(perPersonAmount);
            }
            
            // If payer paid and payee participated (not as payer), payee owes payer (negative balance)
            if (expensePayer.equals(payer) && participants.contains(payee) && !expensePayer.equals(payee)) {
                balance = balance.subtract(perPersonAmount);
            }
        }
        
        // Process settlements
        for (Settlement settlement : settlements) {
            User settlementPayer = settlement.getPayer();
            User settlementPayee = settlement.getPayee();
            BigDecimal amount = settlement.getAmount();
            
            // If payer paid payee in settlement, payer's debt to payee is reduced
            if (settlementPayer.equals(payer) && settlementPayee.equals(payee)) {
                balance = balance.subtract(amount);
            }
            
            // If payee paid payer in settlement, payer's debt to payee is increased
            if (settlementPayer.equals(payee) && settlementPayee.equals(payer)) {
                balance = balance.add(amount);
            }
        }
        
        return balance;
    }
}
