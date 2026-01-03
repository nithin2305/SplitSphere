package com.splitsphere.controller;

import com.splitsphere.dto.ExpenseRequest;
import com.splitsphere.dto.ExpenseResponse;
import com.splitsphere.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = {"http://localhost:4200", "https://splitsphere.netlify.app"})
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(expenseService.createExpense(request, userId));
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }
}
