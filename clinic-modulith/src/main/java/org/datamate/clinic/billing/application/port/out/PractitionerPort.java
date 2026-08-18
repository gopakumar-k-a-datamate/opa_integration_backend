package org.datamate.clinic.billing.application.port.out;

public interface PractitionerPort {
    boolean isPractitionerActive(String practitionerId);
}
