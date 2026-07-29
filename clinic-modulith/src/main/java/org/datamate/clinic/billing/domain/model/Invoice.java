package org.datamate.clinic.billing.domain.model;

public class Invoice {
    private String id;
    private Double totalAmount;
    private Boolean isPaid;
    private String accountType;
    private String invoiceType;
    private String department; 

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
