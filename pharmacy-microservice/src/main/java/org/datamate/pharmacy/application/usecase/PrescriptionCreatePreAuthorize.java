package org.datamate.pharmacy.application.usecase;

import org.datamate.pharmacy.application.dto.CreatePrescriptionPolicyResource;
import org.datamate.pharmacy.application.dto.PatientDto;
import org.datamate.pharmacy.application.dto.PractitionerDto;
import org.datamate.pharmacy.application.port.out.PatientPort;
import org.datamate.pharmacy.application.port.out.PractitionerPort;
import org.springframework.stereotype.Component;

@Component("prescriptionAuthorizor")
public class PrescriptionCreatePreAuthorize {

    private final PractitionerPort practitionerPort;
    private final PatientPort patientPort;
    private final PolicyEnforcer enforcer;


    //
    public void prescriptionCreate() {
        PractitionerDto practioner = practitionerPort.getPractitionerById(payload.practitionerId());

        if(practioner == null) {
            throw new IllegalArgumentException("Practitioner not found");
        }

        CreatePrescriptionPolicyResource resource = new CreatePrescriptionPolicyResource();
        resource.setDoctorLevel(practioner.level());

        PatientDto patient = patientPort.getPatientById(payload.patientId());

        if (patient == null){
            throw new IllegalArgumentException("Patient not found");
        }

        Boolean isSameWard = practioner.ward().equals(patient.ward());
        resource.setIsSameWard(isSameWard);

        enforcer.enforce(resource);
    }
}
