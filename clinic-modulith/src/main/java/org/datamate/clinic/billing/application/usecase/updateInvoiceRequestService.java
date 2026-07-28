package org.datamate.clinic.billing.application.usecase;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.billing.application.dto.UpdateInvoicePolicyResource;
import org.datamate.clinic.billing.application.dto.UpdateInvoiceRequest;
import org.datamate.clinic.billing.application.port.in.UpdateInvoiceServiceUsecase;
import org.springframework.stereotype.Service;

@Service
public class updateInvoiceRequestService implements UpdateInvoiceServiceUsecase {

    private final PolicyEnforcer policyEnforcer;

    public updateInvoiceRequestService(PolicyEnforcer policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }


    @Override
    public String updateInvoice(UpdateInvoiceRequest payload) {

        UpdateInvoicePolicyResource policy=new UpdateInvoicePolicyResource();

        if(payload!=null){
            policy.setTotalAmount(payload.totalAmount());
            policy.setIsPaid(payload.isPaid());
            policyEnforcer.enforce(policy);
        }
        return "invoice updated successfully";
    }
}
