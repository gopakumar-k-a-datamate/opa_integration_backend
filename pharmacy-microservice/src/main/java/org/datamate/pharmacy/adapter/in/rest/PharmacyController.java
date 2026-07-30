package org.datamate.pharmacy.adapter.in.rest;

import org.datamate.pharmacy.application.dto.CreatePrescriptionRequest;
import org.datamate.pharmacy.application.dto.DispenseMedicationRequest;
import org.datamate.pharmacy.application.usecase.DispenseMedicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacy")
public class PharmacyController {

    private final DispenseMedicationService dispenseMedicationService;
    private final org.datamate.pharmacy.application.usecase.CreatePrescriptionService createPrescriptionService;
    private final org.datamate.pharmacy.application.usecase.ReadPrescriptionService readPrescriptionService;

    public PharmacyController(DispenseMedicationService dispenseMedicationService,
                              org.datamate.pharmacy.application.usecase.CreatePrescriptionService createPrescriptionService,
                              org.datamate.pharmacy.application.usecase.ReadPrescriptionService readPrescriptionService) {
        this.dispenseMedicationService = dispenseMedicationService;
        this.createPrescriptionService = createPrescriptionService;
        this.readPrescriptionService = readPrescriptionService;
    }

    @PostMapping("/dispense")
    public ResponseEntity<String> dispenseMedication(@RequestBody DispenseMedicationRequest request) {

        String result = dispenseMedicationService.dispense(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/prescription")
    public ResponseEntity<String> createPrescription(@RequestBody org.datamate.pharmacy.application.dto.CreatePrescriptionRequest request) {
        String result = createPrescriptionService.createPrescription(request);
        return ResponseEntity.ok(result);
    }

    @org.springframework.web.bind.annotation.GetMapping("/prescription")
    public ResponseEntity<java.util.List<String>> readPrescriptions() {
        return ResponseEntity.ok(readPrescriptionService.readPrescriptions());
    }
}
