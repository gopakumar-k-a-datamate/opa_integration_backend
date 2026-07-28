package org.datamate.clinic.billing.application.dto;

public record CreateInvoiceRequest(
    Double totalAmount,
    String insuranceProvider,
    Boolean isPaid,
    Double discountPercentage,
    String dueDate,
    String invoiceType,
    String accountType
) {}
