package org.datamate.finance.domain;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(
    namespace = "finance", 
    name = "journal", 
    action = "approve", 
    description = "Approve a submitted journal entry"
)
public class ApproveJournal {

    @PolicyField(type = FieldType.NUMBER, displayName = "Amount")
    private Double amount;

    @PolicyField(type = FieldType.STRING, displayName = "Department", allowedValues = {"HR", "IT", "FINANCE", "OPERATIONS"})
    private String department;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Requires Audit")
    private Boolean requiresAudit;
}
