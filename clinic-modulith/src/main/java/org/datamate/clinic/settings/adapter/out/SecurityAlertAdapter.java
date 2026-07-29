package org.datamate.clinic.settings.adapter.out;

import org.datamate.clinic.settings.application.port.out.SecurityAlertPort;
import org.springframework.stereotype.Component;

/**
 * Infrastructure implementation of the SecurityAlertPort.
 */
@Component
public class SecurityAlertAdapter implements SecurityAlertPort {

    @Override
    public void sendHighSensitivityAlert(String settingKey, String actionType) {
        // Stub implementation simulating a real-time event/alert being sent
        System.err.println("!!! SECURITY ALERT !!! - High sensitivity setting modified!");
        System.err.println("Action: " + actionType);
        System.err.println("Setting Key: " + settingKey);
        System.err.println("An email has been sent to the super-admin team.");
    }
}
