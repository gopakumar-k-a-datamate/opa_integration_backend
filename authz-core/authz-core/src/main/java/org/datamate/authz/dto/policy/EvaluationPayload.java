package org.datamate.authz.dto.policy;

import java.util.List;
import java.util.Map;

public class EvaluationPayload {
    private Input input;

    public EvaluationPayload() {}

    public EvaluationPayload(Input input) {
        this.input = input;
    }

    public static EvaluationPayload of(Input input) {
        return new EvaluationPayload(input);
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public static class Input {
        private User user;
        private String permission;
        private Map<String, Object> resource;

        public Input() {}

        public Input(User user, String permission, Map<String, Object> resource) {
            this.user = user;
            this.permission = permission;
            this.resource = resource;
        }

        public static Input of(User user, String permission, Map<String, Object> resource) {
            return new Input(user, permission, resource);
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public Map<String, Object> getResource() {
            return resource;
        }

        public void setResource(Map<String, Object> resource) {
            this.resource = resource;
        }
    }

    public static class User {
        private String id;
        private List<String> roles;

        public User() {}

        public User(String id, List<String> roles) {
            this.id = id;
            this.roles = roles;
        }

        public static User of(String id, List<String> roles) {
            return new User(id, roles);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
