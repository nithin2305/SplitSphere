package com.splitsphere.service;

import com.splitsphere.dto.ExpenseRequest;
import com.splitsphere.dto.ExpenseResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final AuditService auditService;
    
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, String payerUserId) {
        User payer = userService.getUserByUserId(payerUserId);
        
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        if (!group.getMembers().contains(payer)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }
        
        Set<User> participants = new HashSet<>();
        for (String participantUserId : request.getParticipantUserIds()) {
            User participant = userService.getUserByUserId(participantUserId);
            if (!group.getMembers().contains(participant)) {
                throw new IllegalArgumentException("Participant " + participantUserId + " is not a member of this group");
            }
            participants.add(participant);
        }
        
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }
        
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPayer(payer);
        expense.setGroup(group);
        expense.setParticipants(participants);
        
        expense = expenseRepository.save(expense);
        
        auditService.log("CREATE", "Expense", expense.getId(), payer, 
                "Expense created: " + expense.getDescription() + " - " + expense.getAmount());
        
        return toExpenseResponse(expense);
    }
    
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        return expenseRepository.findByGroupOrderByCreatedAtDesc(group).stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }
    
    private ExpenseResponse toExpenseResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setPayerName(expense.getPayer().getAccountName());
        response.setPayerUserId(expense.getPayer().getUserId());
        response.setParticipantNames(expense.getParticipants().stream()
                .map(User::getAccountName)
                .collect(Collectors.toList()));
        
        BigDecimal perPersonAmount = expense.getAmount()
                .divide(BigDecimal.valueOf(expense.getParticipants().size()), 2, RoundingMode.HALF_UP);
        response.setPerPersonAmount(perPersonAmount);
        response.setCreatedAt(expense.getCreatedAt());
        
        return response;
    }
}
