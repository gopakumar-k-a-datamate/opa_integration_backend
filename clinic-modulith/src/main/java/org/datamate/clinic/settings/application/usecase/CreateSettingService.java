package org.datamate.clinic.settings.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.enforcement.PolicyEnforcer;
import org.datamate.clinic.settings.application.dto.CreateSettingPolicyResource;
import org.datamate.clinic.settings.application.dto.CreateSettingRequest;
import org.datamate.clinic.settings.application.dto.SettingDto;
import org.datamate.clinic.settings.application.port.in.CreateSettingServiceUsecase;
import org.datamate.clinic.settings.application.port.out.SettingsRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CreateSettingService implements CreateSettingServiceUsecase {

    @EnableLogger
    private Logger log;

    private final PolicyEnforcer policyEnforcer;
    private final SettingsRepositoryPort settingsRepositoryPort;

    public CreateSettingService(PolicyEnforcer policyEnforcer, SettingsRepositoryPort settingsRepositoryPort) {
        this.policyEnforcer = policyEnforcer;
        this.settingsRepositoryPort = settingsRepositoryPort;
    }

    @Override
    public String createSetting(CreateSettingRequest request) {
        // 1. Gather Context (Check if setting already exists)
        log.info("Checking if setting already exists for key: {}", request.key());
        SettingDto existingSetting = settingsRepositoryPort.getSettingByKey(request.key());
        
        if (existingSetting != null) {
            throw new IllegalArgumentException("Setting already exists: " + request.key());
        }

        // 2. Construct the Policy Resource using the requested data
        CreateSettingPolicyResource policyResource = new CreateSettingPolicyResource();
        policyResource.setSensitivityLevel(request.sensitivityLevel());
        policyResource.setSettingCategory(request.settingCategory());
        policyResource.setRequiresRestart(request.requiresRestart());

        // 3. Enforce the Policy
        log.info("Enforcing OPA policy with sensitivity: {}, category: {}, requiresRestart: {}", 
                 request.sensitivityLevel(), request.settingCategory(), request.requiresRestart());
        policyEnforcer.enforce(policyResource);

        // 4. Execute Business Logic (Save)
        SettingDto newSetting = new SettingDto(
                request.key(), 
                request.value(), 
                request.sensitivityLevel(), 
                request.settingCategory(), 
                request.requiresRestart()
        );
        settingsRepositoryPort.saveSetting(newSetting);
        log.info("New setting created successfully in the database.");

        return "Setting created successfully. OPA approved!";
    }
}
