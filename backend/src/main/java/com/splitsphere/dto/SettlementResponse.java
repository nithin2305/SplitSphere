package com.splitsphere.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementResponse {
    private Long id;
    private String payerUserId;
    private String payerName;
    private String payeeUserId;
    private String payeeName;
    private BigDecimal amount;
    private String note;
    private LocalDateTime createdAt;
}
