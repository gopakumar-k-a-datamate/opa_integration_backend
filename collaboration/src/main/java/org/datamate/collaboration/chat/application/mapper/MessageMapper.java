package org.datamate.collaboration.chat.application.mapper;

import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Message domain models to application DTOs.
 */
@Component
public class MessageMapper {

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        return new MessageDto(
                message.getId(),
                message.getSenderId(),
                message.getText(),
                message.isFile(),
                message.isSystemMessage(),
                message.getAttachmentId(),
                message.getTimestamp()
        );
    }
}
