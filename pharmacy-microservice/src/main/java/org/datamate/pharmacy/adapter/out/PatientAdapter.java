package org.datamate.pharmacy.adapter.out;

import org.datamate.pharmacy.application.dto.PatientDto;
import org.datamate.pharmacy.application.port.out.PatientPort;
import org.springframework.stereotype.Component;

@Component
public class PatientAdapter implements PatientPort {

    @Override
    public PatientDto getPatientById(String patientId) {
        // Stub implementation simulating a fetch from an external Patient microservice or DB
        return new PatientDto(patientId, "John Doe", 25);
    }
}
