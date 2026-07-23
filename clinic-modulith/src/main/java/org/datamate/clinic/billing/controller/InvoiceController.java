package org.datamate.clinic.billing.controller;

import org.datamate.clinic.billing.application.InvoiceService;
import org.datamate.clinic.billing.controller.dto.CreateInvoiceRequest;
import org.datamate.clinic.billing.domain.Invoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createInvoice(@RequestBody CreateInvoiceRequest payload) {
        Invoice command = new Invoice();
        command.setTotalAmount(payload.getTotalAmount());
        command.setInsuranceProvider(payload.getInsuranceProvider());
        command.setIsPaid(payload.getIsPaid());
        command.setDiscountPercentage(payload.getDiscountPercentage());
        command.setDueDate(payload.getDueDate());

        String result = invoiceService.createInvoice(command);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }
}
