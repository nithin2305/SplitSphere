package com.splitsphere.controller;

import com.splitsphere.dto.TransactionResponse;
import com.splitsphere.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<TransactionResponse>> getGroupTransactions(@PathVariable Long groupId) {
        return ResponseEntity.ok(transactionService.getGroupTransactions(groupId));
    }
}
