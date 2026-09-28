package org.datamate.collaboration.chat.application.port.in;

import org.datamate.collaboration.chat.application.dto.MessageDto;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import java.util.UUID;

public interface GetMessagesUseCase {
    Paged<MessageDto> getMessages(UUID threadId, PageQuery query);
}
