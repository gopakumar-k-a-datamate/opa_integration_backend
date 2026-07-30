package org.datamate.pharmacy.application.usecase;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.pharmacy.application.dto.CreatePrescriptionPolicyResource;
import org.datamate.pharmacy.application.dto.CreatePrescriptionRequest;
import org.datamate.pharmacy.application.dto.PatientDto;
import org.datamate.pharmacy.application.dto.PractitionerDto;
import org.datamate.pharmacy.application.port.out.PatientPort;
import org.datamate.pharmacy.application.port.out.PractitionerPort;
import org.springframework.stereotype.Service;

@Service
public class CreatePrescriptionService {

    private final PolicyEnforcer policyEnforcer;
    private final PatientPort patientPort;
    private final PractitionerPort practitionerPort;

    public CreatePrescriptionService(PolicyEnforcer policyEnforcer, PatientPort patientPort, PractitionerPort practitionerPort) {
        this.policyEnforcer = policyEnforcer;
        this.patientPort = patientPort;
        this.practitionerPort = practitionerPort;
    }

    public String createPrescription(CreatePrescriptionRequest request) {
        // 1. Business Logic: Fetch Data
        PractitionerDto doc = practitionerPort.getPractitionerById(request.practitionerId());
        PatientDto patient = patientPort.getPatientById(request.patientId());

        if (doc == null || patient == null) {
            throw new IllegalArgumentException("Invalid Practitioner or Patient ID");
        }

        // 2. Pre-Compute Attributes & Assemble Context for OPA
        CreatePrescriptionPolicyResource policyResource = new CreatePrescriptionPolicyResource();
        policyResource.setDoctorLevel(doc.level());
        
        // Business logic evaluates relationship, passes boolean to OPA
        boolean sameWard = doc.ward().equals(patient.ward());
        policyResource.setSameWard(sameWard);

        System.out.println("--- Prescription Request ---");
        System.out.println("Doctor Level: " + doc.level());
        System.out.println("Doctor Ward: " + doc.ward());
        System.out.println("Patient Ward: " + patient.ward());
        System.out.println("Is Same Ward? " + sameWard);

        // 3. Enforce via OPA!
        policyEnforcer.enforce(policyResource);

        // 4. Save (Mocked)
        return "Prescription for " + request.medication() + " successfully created! OPA Approved.";
    }
}
