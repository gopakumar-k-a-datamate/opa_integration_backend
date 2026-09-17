package org.datamate.authz.starter.config;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

public class OpaConfigParser {

    public static String getSchema(Environment env, ResourceLoader resourceLoader) {
        String path = env.getProperty("datamate.authz.opa.config-path", "classpath:opa-config.yaml");
        Resource resource = resourceLoader.getResource(path);
        
        if (resource.exists()) {
            YamlMapFactoryBean yamlFactory = new YamlMapFactoryBean();
            yamlFactory.setResources(resource);
            try {
                Map<String, Object> map = yamlFactory.getObject();
                if (map != null && map.containsKey("database")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dbMap = (Map<String, Object>) map.get("database");
                    if (dbMap != null && dbMap.containsKey("schema")) {
                        return String.valueOf(dbMap.get("schema"));
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors, fall through to default
            }
        }
        return "public"; // Fallback default
    }
}
