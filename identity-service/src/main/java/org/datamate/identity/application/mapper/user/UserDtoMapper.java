package org.datamate.identity.application.mapper.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDtoMapper {

    public UserDto toDto(User entity) {
        if (entity == null) return null;
        return new UserDto(
                entity.getId(),
                entity.getUserName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getReferenceSystem(),
                entity.getReferenceValue(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getStatus(),
                entity.getRoles()
        );
    }
}
