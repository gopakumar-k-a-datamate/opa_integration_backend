package org.datamate.clinic.billing.application.port.in;

import org.datamate.clinic.billing.application.dto.CreateInvoiceRequest;

public interface CreateInvoiceServiceUsecase {
    String createInvoice(CreateInvoiceRequest payload);
}
