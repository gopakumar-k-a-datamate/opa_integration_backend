package org.datamate.clinic.billing.application.port.in;

import org.datamate.clinic.billing.application.dto.UpdateInvoiceRequest;

public interface UpdateInvoiceServiceUsecase {

    String updateInvoice(UpdateInvoiceRequest payload);
}
