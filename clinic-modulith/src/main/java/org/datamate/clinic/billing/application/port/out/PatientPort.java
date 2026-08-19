package org.datamate.clinic.billing.application.port.out;

import java.util.List;

public interface PatientPort {
    boolean hasUnpaidBills(String patientId);
    List<String> getActiveInsurancePolicies(String patientId);
}
