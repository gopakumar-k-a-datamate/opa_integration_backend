package org.datamate.finance.application.port.out;

public interface BudgetPort {
    boolean isBudgetAvailable(String departmentId, Double amount);
}
