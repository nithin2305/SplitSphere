package com.splitsphere.controller;

import com.splitsphere.dto.SettlementRequest;
import com.splitsphere.dto.SettlementResponse;
import com.splitsphere.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {
    
    private final SettlementService settlementService;
    
    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(
            @Valid @RequestBody SettlementRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(settlementService.createSettlement(request, userId));
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId));
    }
}
