package org.datamate.clinic.billing.controller.dto;

public record CreateInvoiceRequest(
    Double totalAmount,
    String insuranceProvider,
    Boolean isPaid,
    Double discountPercentage,
    String dueDate
) {}
