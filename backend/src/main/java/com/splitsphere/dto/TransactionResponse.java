package com.splitsphere.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String type; // "EXPENSE" or "SETTLEMENT"
    private String description;
    private BigDecimal amount;
    private String payerUserId;
    private String payerName;
    private String payeeUserId; // null for expenses
    private String payeeName; // null for expenses
    private String participantNames; // for expenses
    private BigDecimal perPersonAmount; // for expenses
    private String note; // for settlements
    private LocalDateTime createdAt;
}
