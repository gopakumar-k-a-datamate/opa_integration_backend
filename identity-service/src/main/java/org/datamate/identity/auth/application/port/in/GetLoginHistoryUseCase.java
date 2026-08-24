package org.datamate.identity.auth.application.port.in;

import org.datamate.identity.auth.application.dto.LoginHistoryDto;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface GetLoginHistoryUseCase {
    Paged<LoginHistoryDto> getLoginHistory(String username, PageQuery pageQuery);
}


