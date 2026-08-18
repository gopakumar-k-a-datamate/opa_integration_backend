package org.datamate.clinic.settings.application.port.out;

import org.datamate.clinic.settings.application.dto.SettingDto;

public interface SettingsRepositoryPort {
    SettingDto getSettingByKey(String key);
    void saveSetting(SettingDto setting);
}
