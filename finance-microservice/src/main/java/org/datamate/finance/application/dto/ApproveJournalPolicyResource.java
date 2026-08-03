package org.datamate.finance.application.dto;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(
    namespace = "finance", 
    resourceName = "journal", 
    action = "approve", 
    description = "Approve a submitted journal entry"
)
public class ApproveJournalPolicyResource {

    @PolicyField(type = FieldType.NUMBER, displayName = "Amount")
    private Double amount;

    @PolicyField(type = FieldType.STRING, displayName = "Department", allowedValues = {"HR", "IT", "FINANCE", "OPERATIONS"})
    private String department;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Requires Audit")
    private Boolean requiresAudit;
}
