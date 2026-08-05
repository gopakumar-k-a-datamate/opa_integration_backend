package org.datamate.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UpdateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.user.UpdateUserUseCase;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.user.UserAlreadyExistsException;
import org.datamate.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request, String adminUsername) {
        log.info("Starting update of user ID: {} by admin: {}", id, adminUsername);

        User user = userPort.findById(id).orElseThrow(() -> {
            log.error("User update failed. User not found for ID: {}", id);
            return new UserNotFoundException("User not found with id: " + id);
        });

        // Uniqueness validation
        if (userPort.existsByEmailAndIdNot(request.email(), id)) {
            log.error("User update failed. Email '{}' is already taken by another user.", request.email());
            throw new UserAlreadyExistsException("Email '" + request.email() + "' is already taken.");
        }

        User updatedUser = user.updateInformation(
                request.email(),
                request.phoneNumber(),
                request.firstName(),
                request.lastName(),
                request.referenceSystem(),
                request.referenceValue(),
                adminUsername
        );

        User savedUser = userPort.save(updatedUser);

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Successfully updated user ID: {} by admin: {}", id, adminUsername);
        return userDtoMapper.toDto(savedUser);
    }
}
