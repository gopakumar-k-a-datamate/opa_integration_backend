package org.datamate.clinic.settings.application.port.in;

import org.datamate.clinic.settings.application.dto.UpdateSettingRequest;

public interface UpdateSettingServiceUsecase {
    String updateSetting(UpdateSettingRequest request);
}
