package org.datamate.clinic.billing.controller;

import org.datamate.clinic.billing.application.InvoiceService;
import org.datamate.clinic.billing.controller.dto.CreateInvoiceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createInvoice(@RequestBody CreateInvoiceRequest payload) {
        return invoiceService.createInvoice(payload);
    }
}
