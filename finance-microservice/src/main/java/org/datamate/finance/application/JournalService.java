package org.datamate.finance.application;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.finance.application.dto.CreateJournalPolicyResource;
import org.datamate.finance.controller.dto.CreateJournalRequest;
import org.springframework.stereotype.Service;

@Service
public class JournalService {

    private final PolicyEnforcer policyEnforcer;

    public JournalService(PolicyEnforcer policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    /**
     * This method represents a core Use Case in the Application Layer.
     */
    public String createJournal(CreateJournalRequest payload) {
        CreateJournalPolicyResource command = new CreateJournalPolicyResource();
        if (payload != null) {
            command.setAmount(payload.amount());
            command.setDepartment(payload.department());
            command.setCostCenter(payload.costCenter());
            command.setStatus(payload.status());
            command.setRequiresAudit(payload.requiresAudit());
            command.setTransactionDate(payload.transactionDate());
        }

        // Demonstrate programmatic SDK policy enforcement:
        policyEnforcer.enforce(command);

        // Business logic to save the journal to the database would go here.
        return "Service execution successful. OPA approved the command!";
    }
}
