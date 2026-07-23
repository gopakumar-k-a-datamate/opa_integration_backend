package org.datamate.clinic.billing.domain;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "billing", name = "invoice", action = "create", description = "Create patient invoice")
public class Invoice {

    @PolicyField(type = FieldType.NUMBER, displayName = "Total Amount")
    private Double totalAmount;

    @PolicyField(type = FieldType.STRING, displayName = "Insurance Provider", allowedValues = {"BLUE_CROSS", "MEDICARE", "AETNA", "CIGNA", "UNINSURED"})
    private String insuranceProvider;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Is Fully Paid")
    private Boolean isPaid;

    @PolicyField(type = FieldType.NUMBER, displayName = "Discount Percentage")
    private Double discountPercentage;

    @PolicyField(type = FieldType.DATE, displayName = "Due Date")
    private String dueDate;

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
