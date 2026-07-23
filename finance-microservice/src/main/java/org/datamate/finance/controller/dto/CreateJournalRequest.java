package org.datamate.finance.controller.dto;

public class CreateJournalRequest {
    private Double amount;
    private String department;
    private String costCenter;
    private String status;
    private Boolean requiresAudit;
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
