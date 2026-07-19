package org.datamate.finance.domain;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "finance", name = "journal", action = "create", description = "Create new journal entries")
public class Journal {

    @PolicyField(type = FieldType.NUMBER, displayName = "Amount")
    private Double amount;

    @PolicyField(type = FieldType.STRING, displayName = "Department", allowedValues = {"HR", "IT", "FINANCE", "OPERATIONS"})
    private String department;

    @PolicyField(type = FieldType.STRING, displayName = "Cost Center")
    private String costCenter;

    @PolicyField(type = FieldType.STRING, displayName = "Status", allowedValues = {"DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED"})
    private String status;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Requires Audit")
    private Boolean requiresAudit;

    @PolicyField(type = FieldType.DATE, displayName = "Transaction Date")
    private String transactionDate;

    // Standard getters/setters would go here
}
