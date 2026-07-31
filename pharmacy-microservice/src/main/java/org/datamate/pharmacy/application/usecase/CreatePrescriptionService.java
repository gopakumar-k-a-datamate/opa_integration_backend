package org.datamate.pharmacy.application.usecase;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.pharmacy.application.dto.CreatePrescriptionPolicyResource;
import org.datamate.pharmacy.application.dto.CreatePrescriptionRequest;
import org.datamate.pharmacy.application.dto.PatientDto;
import org.datamate.pharmacy.application.dto.PractitionerDto;
import org.datamate.pharmacy.application.port.out.PatientPort;
import org.datamate.pharmacy.application.port.out.PractitionerPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class CreatePrescriptionService {

    private final PractitionerPort practitionerPort;
    private final PatientPort patientPort;
    private final PolicyEnforcer enforcer;

    public CreatePrescriptionService(PractitionerPort practitionerPort,
                                     PatientPort patientPort,
                                     PolicyEnforcer enforcer) {
        this.practitionerPort = practitionerPort;
        this.patientPort = patientPort;
        this.enforcer = enforcer;
    }


    @PreAuthorize("prescriptionAuthorizor.prescriptionCreate()")
    public String createPrescription(CreatePrescriptionRequest payload) {
        return "Prescription created ";
    }
}
