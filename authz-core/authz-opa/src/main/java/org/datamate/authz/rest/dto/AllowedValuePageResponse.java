package org.datamate.authz.rest.dto;

import java.util.List;

public record AllowedValuePageResponse(
        List<AllowedValueResponse> content,
        int page,
        int size,
        long totalElements,
        boolean last
) {}
