package org.datamate.clinic.settings.application.port.out;

public interface SecurityAlertPort {
    void sendHighSensitivityAlert(String settingKey, String actionType);
}
