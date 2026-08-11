package org.datamate.pharmacy.adapter.in.rest;


import org.datamate.authz.annotation.ProtectedResource;
import org.datamate.pharmacy.application.dto.CreatePrescriptionRequest;
import org.datamate.pharmacy.application.usecase.CreatePrescriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pharmacy")
public class PrescriptionController {

    private final CreatePrescriptionService createPrescription;

    public PrescriptionController(CreatePrescriptionService createPrescription) {
        this.createPrescription = createPrescription;
    }


    @ProtectedResource("pharmacy:prescription:write")
    @PostMapping("/prescription")
    public String createPrescription(@RequestBody CreatePrescriptionRequest body){
        return createPrescription.createPrescription(body);
    }
}
