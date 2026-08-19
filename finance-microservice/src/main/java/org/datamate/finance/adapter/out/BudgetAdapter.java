package org.datamate.finance.adapter.out;

import org.datamate.finance.application.port.out.BudgetPort;
import org.springframework.stereotype.Component;

@Component
public class BudgetAdapter implements BudgetPort {
    @Override
    public boolean isBudgetAvailable(String departmentId, Double amount) {
        // Dummy implementation returning mapped value
        return true;
    }
}
