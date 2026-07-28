package org.datamate.finance.adapter.out;

import org.datamate.finance.application.port.out.CostCenterPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CostCenterAdapter implements CostCenterPort {
    @Override
    public String getCostCenterStatus(String costCenterId) {
        return "ACTIVE";
    }

    @Override
    public List<String> getAllowedDepartments(String costCenterId) {
        // Dummy implementation returning mapped list of values
        return List.of("HR", "IT", "FINANCE");
    }
}
