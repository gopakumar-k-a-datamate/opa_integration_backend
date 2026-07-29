package org.datamate.clinic.settings.application.port.in;

import org.datamate.clinic.settings.application.dto.CreateSettingRequest;

public interface CreateSettingServiceUsecase {
    String createSetting(CreateSettingRequest request);
}
