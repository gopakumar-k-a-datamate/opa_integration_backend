package org.datamate.clinic.billing.application;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.billing.domain.Invoice;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final PolicyEnforcer policyEnforcer;

    public InvoiceService(PolicyEnforcer policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    public String createInvoice(Invoice command) {
        // Demonstrate programmatic SDK policy enforcement:
        policyEnforcer.enforce(command);
        return "Service execution successful. OPA approved the command!";
    }
}
