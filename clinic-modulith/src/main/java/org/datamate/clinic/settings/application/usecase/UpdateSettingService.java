package org.datamate.clinic.settings.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.settings.application.dto.SettingDto;
import org.datamate.clinic.settings.application.dto.UpdateSettingPolicyResource;
import org.datamate.clinic.settings.application.dto.UpdateSettingRequest;
import org.datamate.clinic.settings.application.port.in.UpdateSettingServiceUsecase;
import org.datamate.clinic.settings.application.port.out.SecurityAlertPort;
import org.datamate.clinic.settings.application.port.out.SettingsRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UpdateSettingService implements UpdateSettingServiceUsecase {

    @EnableLogger
    private Logger log;

    private final PolicyEnforcer policyEnforcer;
    private final SettingsRepositoryPort settingsRepositoryPort;
    private final SecurityAlertPort securityAlertPort;

    public UpdateSettingService(PolicyEnforcer policyEnforcer, 
                                SettingsRepositoryPort settingsRepositoryPort, 
                                SecurityAlertPort securityAlertPort) {
        this.policyEnforcer = policyEnforcer;
        this.settingsRepositoryPort = settingsRepositoryPort;
        this.securityAlertPort = securityAlertPort;
    }

    @Override
    public String updateSetting(UpdateSettingRequest request) {
        // 1. Fetching Context Before Authorization
        log.info("Fetching existing setting from database for key: {}", request.settingKey());
        SettingDto existingSetting = settingsRepositoryPort.getSettingByKey(request.settingKey());
        
        if (existingSetting == null) {
            throw new IllegalArgumentException("Setting not found: " + request.settingKey());
        }

        // 2. Construct the Policy Resource using the securely fetched context
        UpdateSettingPolicyResource policyResource = new UpdateSettingPolicyResource();
        policyResource.setSensitivityLevel(existingSetting.sensitivityLevel());
        policyResource.setSettingCategory(existingSetting.settingCategory());
        policyResource.setRequiresRestart(existingSetting.requiresRestart());

        // 3. Enforce the Policy
        // OPA Rule evaluates: Can this user 'update' a setting with these attributes?
        log.info("Enforcing OPA policy with fetched sensitivity: {}, category: {}, requiresRestart: {}", 
                 existingSetting.sensitivityLevel(), existingSetting.settingCategory(), existingSetting.requiresRestart());
        policyEnforcer.enforce(policyResource);

        // 4. Execute Business Logic (Update and Save)
        SettingDto updatedSetting = new SettingDto(
                existingSetting.key(),
                request.newValue(),
                existingSetting.sensitivityLevel(),
                existingSetting.settingCategory(),
                existingSetting.requiresRestart()
        );
        settingsRepositoryPort.saveSetting(updatedSetting);
        log.info("Setting updated successfully in the database.");

        // 5. Real-Time Logic (Trigger alert if needed)
        if ("HIGH".equalsIgnoreCase(updatedSetting.sensitivityLevel())) {
            log.info("High sensitivity setting modified. Triggering real-time security alert.");
            securityAlertPort.sendHighSensitivityAlert(updatedSetting.key(), "UPDATE");
        }

        return "Setting updated successfully. OPA approved!";
    }
}
