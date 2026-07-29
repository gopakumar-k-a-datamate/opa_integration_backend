package org.datamate.pharmacy.application.usecase;

import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.pharmacy.application.dto.DispenseMedicationPolicyResource;
import org.datamate.pharmacy.application.dto.DispenseMedicationRequest;
import org.datamate.pharmacy.application.dto.MedicationDto;
import org.datamate.pharmacy.application.dto.PatientDto;
import org.datamate.pharmacy.application.port.out.InventoryAlertPort;
import org.datamate.pharmacy.application.port.out.MedicationPort;
import org.datamate.pharmacy.application.port.out.PatientPort;
import org.springframework.stereotype.Service;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

@Service
public class DispenseMedicationService {

    @EnableLogger
    private Logger log;

    private final PolicyEnforcer policyEnforcer;
    private final MedicationPort medicationPort;
    private final PatientPort patientPort;
    private final InventoryAlertPort inventoryAlertPort;

    public DispenseMedicationService(
            PolicyEnforcer policyEnforcer,
            MedicationPort medicationPort,
            PatientPort patientPort,
            InventoryAlertPort inventoryAlertPort) {
        this.policyEnforcer = policyEnforcer;
        this.medicationPort = medicationPort;
        this.patientPort = patientPort;
        this.inventoryAlertPort = inventoryAlertPort;
    }

    public String dispense(DispenseMedicationRequest request) {
        // 1. Gather Context
        MedicationDto medication = medicationPort.getMedicationById(request.medicationId());
        PatientDto patient = patientPort.getPatientById(request.patientId());

        if (medication == null || patient == null) {
            throw new IllegalArgumentException("Invalid medication or patient");
        }

        if (medication.getCurrentStock() < request.quantity()) {
            throw new IllegalStateException("Insufficient stock to dispense");
        }

        log.info("Gathered Context -> Drug Class: {}, Patient Age: {}", medication.getDrugClass(), patient.age());

        // 2. Build Policy Resource
        DispenseMedicationPolicyResource policyResource = new DispenseMedicationPolicyResource();
        policyResource.setDrugClass(medication.getDrugClass());
        policyResource.setPatientAge(patient.age());

        // 3. Enforce OPA Authorization
        // Evaluates: Can the current user dispense a drug of this class to a patient of this age?
        log.info("Enforcing OPA policy for pharmacy:medication:dispense...");
        policyEnforcer.enforce(policyResource);

        // 4. Execute Business Logic (State Mutation)
        medication.setCurrentStock(medication.getCurrentStock() - request.quantity());
        medicationPort.saveMedication(medication);
        
        log.info("Successfully dispensed {} units of {} to {}", 
                request.quantity(), medication.getName(), patient.name());

        // 5. Real-Time Side Effect Logic
        if (medication.getCurrentStock() <= medication.getMinimumStockThreshold()) {
            log.warn("Stock fell below threshold. Triggering inventory alert.");
            inventoryAlertPort.sendLowStockAlert(medication.getId(), medication.getCurrentStock());
        }

        return "Dispensed successfully! OPA approved.";
    }
}
