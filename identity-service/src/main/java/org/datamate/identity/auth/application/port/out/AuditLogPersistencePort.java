package org.datamate.identity.auth.application.port.out;

import org.datamate.identity.auth.application.dto.LoginHistoryDto;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface AuditLogPersistencePort {
    Paged<LoginHistoryDto> findLoginHistory(String username, PageQuery pageQuery);
}


