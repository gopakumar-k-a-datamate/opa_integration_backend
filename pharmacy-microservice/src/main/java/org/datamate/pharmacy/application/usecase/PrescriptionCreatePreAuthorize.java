package org.datamate.pharmacy.application.usecase;

import org.springframework.stereotype.Component;

@Component("prescriptionAuthorizor")
public class PrescriptionCreatePreAuthorize {

    public String Prescription() {
        return "Prescription created ";
    }
}
