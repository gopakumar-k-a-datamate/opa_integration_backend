package org.datamate.clinic.billing.adapter.out;

import org.datamate.clinic.billing.application.port.out.PractitionerPort;
import org.springframework.stereotype.Component;

@Component
public class PractitionerAdapter implements PractitionerPort {
    @Override
    public boolean isPractitionerActive(String practitionerId) {
        // Dummy implementation returning mapped value
        return true;
    }
}
