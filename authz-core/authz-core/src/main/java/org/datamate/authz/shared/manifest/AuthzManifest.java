package org.datamate.authz.shared.manifest;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;

import java.util.List;

public class AuthzManifest {
    private List<ResourceManifest> resources;

    public AuthzManifest() {}

    public AuthzManifest(List<ResourceManifest> resources) {
        this.resources = resources;
    }

    public List<ResourceManifest> getResources() {
        return resources;
    }

    public void setResources(List<ResourceManifest> resources) {
        this.resources = resources;
    }

    public static class ResourceManifest {
        private String namespace;
        private String name;
        private String action;
        private String description;
        private List<FieldManifest> fields;

        public ResourceManifest() {}

        public ResourceManifest(String namespace, String name, String action, String description, List<FieldManifest> fields) {
            this.namespace = namespace;
            this.name = name;
            this.action = action;
            this.description = description;
            this.fields = fields;
        }

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<FieldManifest> getFields() { return fields; }
        public void setFields(List<FieldManifest> fields) { this.fields = fields; }
    }

    public static class FieldManifest {
        private String fieldName;
        private String displayName;
        private FieldType type;
        private List<String> allowedValues;
        private String optionsEndpoint;

        public FieldManifest() {}

        public FieldManifest(String fieldName, String displayName, FieldType type, List<String> allowedValues, String optionsEndpoint) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.type = type;
            this.allowedValues = allowedValues;
            this.optionsEndpoint = optionsEndpoint;
        }

        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public FieldType getType() { return type; }
        public void setType(FieldType type) { this.type = type; }

        public List<String> getAllowedValues() { return allowedValues; }
        public void setAllowedValues(List<String> allowedValues) { this.allowedValues = allowedValues; }

        public String getOptionsEndpoint() { return optionsEndpoint; }
        public void setOptionsEndpoint(String optionsEndpoint) { this.optionsEndpoint = optionsEndpoint; }
    }
}
