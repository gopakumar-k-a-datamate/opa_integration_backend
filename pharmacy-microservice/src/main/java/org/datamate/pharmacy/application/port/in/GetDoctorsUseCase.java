package org.datamate.pharmacy.application.port.in;

import org.datamate.authz.rest.dto.AllowedValuePageResponse;

/**
 * Inbound Port (Use Case Interface) for getting doctors.
 * Defines the operations the outside world (like a REST controller)
 * can trigger within the application layer.
 */
public interface GetDoctorsUseCase {
    AllowedValuePageResponse execute(int page, int size, String search);
}
