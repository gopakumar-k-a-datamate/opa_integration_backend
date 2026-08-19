package org.datamate.pharmacy.application.port.in;

import org.datamate.authz.rest.dto.AllowedValuePageResponse;

public interface GetDoctorsUseCase {
    AllowedValuePageResponse execute(int page, int size, String search);
}
