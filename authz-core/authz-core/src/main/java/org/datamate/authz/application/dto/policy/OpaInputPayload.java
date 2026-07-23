package org.datamate.authz.application.dto.policy;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OpaInputPayload {
    private Input input;

    @Data
    @Builder
    public static class Input {
        private User user;
        private String permission;
        private Map<String, Object> resource;
    }

    @Data
    @Builder
    public static class User {
        private String id;
        private List<String> roles;
    }
}
