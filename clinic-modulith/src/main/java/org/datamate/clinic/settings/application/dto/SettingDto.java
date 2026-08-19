package org.datamate.clinic.settings.application.dto;

public record SettingDto(
    String key,
    String value,
    String sensitivityLevel,
    String settingCategory,
    Boolean requiresRestart
) {}
