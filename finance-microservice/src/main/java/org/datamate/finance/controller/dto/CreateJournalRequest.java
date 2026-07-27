package org.datamate.finance.controller.dto;

public record CreateJournalRequest(
    Double amount,
    String department,
    String costCenter,
    String status,
    Boolean requiresAudit,
    String transactionDate
) {}
