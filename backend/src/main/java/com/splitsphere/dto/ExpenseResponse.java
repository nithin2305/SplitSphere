package com.splitsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String payerName;
    private String payerUserId;
    private List<String> participantNames;
    private BigDecimal perPersonAmount;
    private LocalDateTime createdAt;
}
