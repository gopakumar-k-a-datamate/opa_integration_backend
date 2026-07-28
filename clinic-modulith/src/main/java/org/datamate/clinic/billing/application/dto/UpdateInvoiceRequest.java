package org.datamate.clinic.billing.application.dto;

public record UpdateInvoiceRequest (
    Double totalAmount,
    Boolean isPaid
){}
