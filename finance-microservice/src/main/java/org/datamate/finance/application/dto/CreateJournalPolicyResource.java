package org.datamate.finance.application.dto;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "finance", resourceName = "journal", action = "create", description = "Create new journal entries")
public class CreateJournalPolicyResource {

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

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getRequiresAudit() { return requiresAudit; }
    public void setRequiresAudit(Boolean requiresAudit) { this.requiresAudit = requiresAudit; }
    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
}
