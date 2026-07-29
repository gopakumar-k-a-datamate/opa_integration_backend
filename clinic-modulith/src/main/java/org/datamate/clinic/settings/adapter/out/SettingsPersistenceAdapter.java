package org.datamate.clinic.settings.adapter.out;

import org.datamate.clinic.settings.application.dto.SettingDto;
import org.datamate.clinic.settings.application.port.out.SettingsRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Infrastructure implementation of the SettingsRepositoryPort.
 * Uses an in-memory map for demonstration purposes.
 */
@Component
public class SettingsPersistenceAdapter implements SettingsRepositoryPort {

    private final Map<String, SettingDto> inMemoryDatabase = new ConcurrentHashMap<>();

    public SettingsPersistenceAdapter() {
        // Pre-populate some data for update demonstrations
        inMemoryDatabase.put("SMTP_PASSWORD", new SettingDto("SMTP_PASSWORD", "super_secret", "HIGH", "SECURITY", false));
        inMemoryDatabase.put("WELCOME_MESSAGE", new SettingDto("WELCOME_MESSAGE", "Welcome to the Clinic", "LOW", "GENERAL", false));
    }

    @Override
    public SettingDto getSettingByKey(String key) {
        return inMemoryDatabase.get(key);
    }

    @Override
    public void saveSetting(SettingDto setting) {
        inMemoryDatabase.put(setting.key(), setting);
        System.out.println("Saved setting to in-memory DB: " + setting.key());
    }
}
