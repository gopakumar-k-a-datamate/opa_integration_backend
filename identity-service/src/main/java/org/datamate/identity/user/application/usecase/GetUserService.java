package org.datamate.identity.user.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.user.application.dto.UserDto;
import org.datamate.identity.user.application.mapper.UserDtoMapper;
import org.datamate.identity.user.application.port.in.GetUserUseCase;
import org.datamate.identity.user.application.port.out.UserPersistencePort;
import org.datamate.identity.user.domain.exception.UserNotFoundException;
import org.datamate.identity.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        log.info("Starting retrieval of user details for ID: {}", id);
        
        User user = userPort.findById(id).orElseThrow(() -> {
            log.error("User details retrieval failed. User not found for ID: {}", id);
            return new UserNotFoundException();
        });

        log.info("Successfully retrieved user details for ID: {}", id);
        return userDtoMapper.toDto(user);
    }
}


