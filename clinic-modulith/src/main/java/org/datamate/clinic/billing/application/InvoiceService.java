package org.datamate.clinic.billing.application;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.billing.application.dto.CreateInvoicePolicyResource;
import org.datamate.clinic.billing.controller.dto.CreateInvoiceRequest;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final PolicyEnforcer policyEnforcer;

    public InvoiceService(PolicyEnforcer policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    public String createInvoice(CreateInvoiceRequest payload) {
        CreateInvoicePolicyResource command = new CreateInvoicePolicyResource();
        if (payload != null) {
            command.setTotalAmount(payload.totalAmount());
            command.setInsuranceProvider(payload.insuranceProvider());
            command.setIsPaid(payload.isPaid());
            command.setDiscountPercentage(payload.discountPercentage());
            command.setDueDate(payload.dueDate());
        }

        // Demonstrate programmatic SDK policy enforcement:
        policyEnforcer.enforce(command);
        return "Service execution successful. OPA approved the command!";
    }
}
