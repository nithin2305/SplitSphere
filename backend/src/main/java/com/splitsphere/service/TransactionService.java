package com.splitsphere.service;

import com.splitsphere.dto.TransactionResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getGroupTransactions(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        List<TransactionResponse> transactions = new ArrayList<>();
        
        // Add expenses
        List<Expense> expenses = expenseRepository.findByGroupOrderByCreatedAtDesc(group);
        for (Expense expense : expenses) {
            TransactionResponse response = new TransactionResponse();
            response.setId(expense.getId());
            response.setType("EXPENSE");
            response.setDescription(expense.getDescription());
            response.setAmount(expense.getAmount());
            response.setPayerUserId(expense.getPayer().getUserId());
            response.setPayerName(expense.getPayer().getAccountName());
            response.setParticipantNames(expense.getParticipants().stream()
                    .map(User::getAccountName)
                    .collect(Collectors.joining(", ")));
            
            BigDecimal perPersonAmount = expense.getAmount()
                    .divide(BigDecimal.valueOf(expense.getParticipants().size()), 2, RoundingMode.HALF_UP);
            response.setPerPersonAmount(perPersonAmount);
            response.setCreatedAt(expense.getCreatedAt());
            transactions.add(response);
        }
        
        // Add settlements
        List<Settlement> settlements = settlementRepository.findByGroupOrderByCreatedAtDesc(group);
        for (Settlement settlement : settlements) {
            TransactionResponse response = new TransactionResponse();
            response.setId(settlement.getId());
            response.setType("SETTLEMENT");
            response.setDescription("Payment from " + settlement.getPayer().getAccountName() + " to " + settlement.getPayee().getAccountName());
            response.setAmount(settlement.getAmount());
            response.setPayerUserId(settlement.getPayer().getUserId());
            response.setPayerName(settlement.getPayer().getAccountName());
            response.setPayeeUserId(settlement.getPayee().getUserId());
            response.setPayeeName(settlement.getPayee().getAccountName());
            response.setNote(settlement.getNote());
            response.setCreatedAt(settlement.getCreatedAt());
            transactions.add(response);
        }
        
        // Sort by date descending
        transactions.sort(Comparator.comparing(TransactionResponse::getCreatedAt).reversed());
        
        return transactions;
    }
}
