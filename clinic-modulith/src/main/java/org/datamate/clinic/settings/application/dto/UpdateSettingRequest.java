package org.datamate.clinic.settings.application.dto;

public record UpdateSettingRequest(
    String settingKey,
    String newValue
) {}
