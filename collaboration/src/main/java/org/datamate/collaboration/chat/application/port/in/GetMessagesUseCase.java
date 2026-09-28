package org.datamate.collaboration.chat.application.port.in;

import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface GetMessagesUseCase {
    Page<MessageDto> getMessages(UUID threadId, Pageable pageable);
}
