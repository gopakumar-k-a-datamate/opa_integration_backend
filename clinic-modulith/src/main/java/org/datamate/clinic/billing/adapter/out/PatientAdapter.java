package org.datamate.clinic.billing.adapter.out;

import org.datamate.clinic.billing.application.port.out.PatientPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientAdapter implements PatientPort {
    @Override
    public boolean hasUnpaidBills(String patientId) {
        return false;
    }

    @Override
    public List<String> getActiveInsurancePolicies(String patientId) {
        // Dummy implementation returning mapped list of values
        return List.of("MEDICARE", "BLUE_CROSS");
    }
}
