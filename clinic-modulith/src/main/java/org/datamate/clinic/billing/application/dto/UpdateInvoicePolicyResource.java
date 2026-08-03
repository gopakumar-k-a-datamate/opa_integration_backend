package org.datamate.clinic.billing.application.dto;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;

@PolicyResource(namespace = "billing", resourceName = "invoice", action = "update", description = "Update patient invoice")
public class UpdateInvoicePolicyResource {

    @PolicyField(type = FieldType.NUMBER, displayName = "Total Amount")
    private Double totalAmount;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Is Fully Paid")
    private Boolean isPaid;

    @PolicyField(type = FieldType.STRING, displayName = "Account Type", allowedValues = {"INDIVIDUAL_DEBTOR", "CORPORATE_DEBTOR", "INSURANCE_CLAIM", "CASH_ACCOUNT"})
    private String accountType;

    @PolicyField(type = FieldType.STRING, displayName = "Invoice Type", allowedValues = {"OUTPATIENT", "INPATIENT", "PHARMACY", "EMERGENCY"})
    private String invoiceType;

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
}
