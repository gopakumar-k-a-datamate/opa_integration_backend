package org.datamate.clinic.billing.application;

import org.datamate.clinic.billing.domain.Invoice;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {
    public String createInvoice(Invoice command) {
        return "Service execution successful. OPA approved the command!";
    }
}
