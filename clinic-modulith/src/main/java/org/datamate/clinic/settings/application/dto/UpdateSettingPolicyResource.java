package org.datamate.clinic.settings.application.dto;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "clinical", resourceName = "settings", action = "update", description = "Update clinical settings")
public class UpdateSettingPolicyResource {

    @PolicyField(type = FieldType.STRING, displayName = "Sensitivity Level", allowedValues = {"LOW", "MEDIUM", "HIGH"})
    private String sensitivityLevel;

    @PolicyField(type = FieldType.STRING, displayName = "Setting Category", allowedValues = {"GENERAL", "BILLING", "SECURITY", "SYSTEM"})
    private String settingCategory;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Requires Restart")
    private Boolean requiresRestart;

    public String getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(String sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }

    public String getSettingCategory() {
        return settingCategory;
    }

    public void setSettingCategory(String settingCategory) {
        this.settingCategory = settingCategory;
    }

    public Boolean getRequiresRestart() {
        return requiresRestart;
    }

    public void setRequiresRestart(Boolean requiresRestart) {
        this.requiresRestart = requiresRestart;
    }
}
