package org.datamate.clinic.billing.adapter.in.controller;

import org.datamate.clinic.billing.application.dto.UpdateInvoiceRequest;
import org.datamate.clinic.billing.application.port.in.CreateInvoiceServiceUsecase;
import org.datamate.clinic.billing.application.port.in.UpdateInvoiceServiceUsecase;
import org.datamate.clinic.billing.application.usecase.CreateInvoiceService;
import org.datamate.clinic.billing.application.dto.CreateInvoiceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final CreateInvoiceServiceUsecase createInvoiceServiceUsecase;
    private final UpdateInvoiceServiceUsecase updateInvoiceServiceUsecase;

    public InvoiceController(CreateInvoiceService createInvoiceService, UpdateInvoiceServiceUsecase updateInvoiceServiceUsecase) {
        this.createInvoiceServiceUsecase = createInvoiceService;
        this.updateInvoiceServiceUsecase = updateInvoiceServiceUsecase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createInvoice(@RequestBody CreateInvoiceRequest payload) {
        return createInvoiceServiceUsecase.createInvoice(payload);
    }

}
