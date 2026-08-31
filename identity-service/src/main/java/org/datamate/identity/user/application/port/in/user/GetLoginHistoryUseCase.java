package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.LoginHistoryDto;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface GetLoginHistoryUseCase {
    Paged<LoginHistoryDto> getLoginHistory(String username, PageQuery pageQuery);
}
