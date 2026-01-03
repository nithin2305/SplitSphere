package com.splitsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private String userId;
    private String userName;
    private BigDecimal balance;
    private String status; // "owes" or "owed"
}
