package org.datamate.finance.application.port.out;

import java.util.List;

public interface CostCenterPort {
    String getCostCenterStatus(String costCenterId);
    List<String> getAllowedDepartments(String costCenterId);
}
