package org.datamate.identity.auth.application.port.out.audit;

import org.datamate.identity.user.application.dto.user.LoginHistoryDto;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface AuditLogPersistencePort {
    Paged<LoginHistoryDto> findLoginHistory(String username, PageQuery pageQuery);
}
