package com.splitsphere.controller;

import com.splitsphere.dto.BalanceResponse;
import com.splitsphere.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = {"http://localhost:4200", "https://splitsphere.netlify.app"})
public class BalanceController {
    
    private final BalanceService balanceService;
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(
            @PathVariable Long groupId,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(balanceService.getGroupBalances(groupId, userId));
    }
}
