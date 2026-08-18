package org.datamate.finance.application;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.finance.application.dto.CreateJournalPolicyResource;
import org.datamate.finance.application.dto.CreateJournalRequest;
import org.datamate.finance.application.port.out.CostCenterPort;
import org.datamate.finance.application.port.out.BudgetPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {

    @EnableLogger
    private Logger log;

    private final PolicyEnforcer policyEnforcer;
    private final CostCenterPort costCenterPort;
    private final BudgetPort budgetPort;

    public JournalService(PolicyEnforcer policyEnforcer, CostCenterPort costCenterPort, BudgetPort budgetPort) {
        this.policyEnforcer = policyEnforcer;
        this.costCenterPort = costCenterPort;
        this.budgetPort = budgetPort;
    }

    public String createJournal(CreateJournalRequest payload) {
        // 1. Fetching related entities and lists from ports BEFORE authorization
        log.info("Fetching Cost Center details from port...");
        String costCenterStatus = costCenterPort.getCostCenterStatus(payload.costCenter());
        List<String> allowedDepartments = costCenterPort.getAllowedDepartments(payload.costCenter());
        
        log.info("Validating department budgets from port...");
        boolean isBudgetAvailable = budgetPort.isBudgetAvailable(payload.department(), payload.amount());

        // 2. Perform some preliminary business operations and list processing
        if (!isBudgetAvailable) {
            throw new IllegalStateException("Budget exhausted for department.");
        }
        if (!"ACTIVE".equals(costCenterStatus)) {
            throw new IllegalStateException("Cost Center is not active.");
        }
        
        // Processing the List returned from the adapter
        boolean isDepartmentAllowed = false;
        for (String dept : allowedDepartments) {
            if (dept.equalsIgnoreCase(payload.department())) {
                isDepartmentAllowed = true;
                break;
            }
        }
        
        if (!isDepartmentAllowed) {
            throw new IllegalArgumentException("Department " + payload.department() + " is not authorized for Cost Center " + payload.costCenter());
        }

        // 3. Construct the Policy Resource
        CreateJournalPolicyResource command = new CreateJournalPolicyResource();
        if (payload != null) {
            command.setAmount(payload.amount());
            command.setDepartment(payload.department());
            command.setCostCenter(payload.costCenter());
            command.setStatus(payload.status());
            command.setRequiresAudit(payload.requiresAudit());
            command.setTransactionDate(payload.transactionDate());
        }

        // 4. Finally, Enforce the Policy at the exact right moment
        log.info("Enforcing OPA policy with gathered context...");
        policyEnforcer.enforce(command);

        // 5. Final business logic after approval
        return "Journal entry created successfully. OPA approved the command!";
    }
}
