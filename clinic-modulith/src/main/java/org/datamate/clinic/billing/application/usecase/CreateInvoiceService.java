package org.datamate.clinic.billing.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.billing.application.dto.CreateInvoicePolicyResource;
import org.datamate.clinic.billing.application.dto.CreateInvoiceRequest;
import org.datamate.clinic.billing.application.port.in.CreateInvoiceServiceUsecase;
import org.datamate.clinic.billing.application.port.out.PractitionerPort;
import org.datamate.clinic.billing.application.port.out.PatientPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateInvoiceService implements CreateInvoiceServiceUsecase {

    @EnableLogger
    private Logger log;

    private final PolicyEnforcer policyEnforcer;
    private final PractitionerPort practitionerPort;
    private final PatientPort patientPort;

    public CreateInvoiceService(PolicyEnforcer policyEnforcer, PractitionerPort practitionerPort, PatientPort patientPort) {
        this.policyEnforcer = policyEnforcer;
        this.practitionerPort = practitionerPort;
        this.patientPort = patientPort;
    }

    @Override
    public String createInvoice(CreateInvoiceRequest payload) {
        // 1. Fetching related entities from ports BEFORE authorization
        log.info("Fetching practitioner details from port...");
        String practitionerId = "DOC-123";
        boolean isPractitionerActive = practitionerPort.isPractitionerActive(practitionerId);
        
        log.info("Fetching patient history from port...");
        String patientId = "PAT-456";
        boolean hasUnpaidBills = patientPort.hasUnpaidBills(patientId);
        List<String> activePolicies = patientPort.getActiveInsurancePolicies(patientId);

        // 2. Perform some preliminary business logic
        if (!isPractitionerActive) {
            throw new IllegalStateException("Practitioner is not active.");
        }

        boolean hasValidInsurance = false;
        if (payload != null && payload.insuranceProvider() != null) {
            for (String policy : activePolicies) {
                if (policy.equalsIgnoreCase(payload.insuranceProvider())) {
                    hasValidInsurance = true;
                    break;
                }
            }
        }
        
        if (!hasValidInsurance && payload != null && !"UNINSURED".equalsIgnoreCase(payload.insuranceProvider())) {
            throw new IllegalArgumentException("Patient does not have an active policy with " + payload.insuranceProvider());
        }

        // 3. Construct the Policy Resource using both Request payload AND fetched contextual data
        CreateInvoicePolicyResource command = new CreateInvoicePolicyResource();
        if (payload != null) {
            command.setTotalAmount(payload.totalAmount());
            command.setInsuranceProvider(payload.insuranceProvider());
            command.setIsPaid(payload.isPaid());
            command.setDiscountPercentage(payload.discountPercentage());
            command.setDueDate(payload.dueDate());
            command.setInvoiceType(payload.invoiceType());
            command.setAccountType(payload.accountType());
        }

        // 4. Finally, Enforce the Policy at the exact right moment
        log.info("Enforcing OPA policy with gathered context...");
        policyEnforcer.enforce(command);

        // 5. Final business logic after approval
        return "Invoice created successfully for practitioner " + practitionerId + ". OPA approved!";
    }
}
