package org.datamate.authz.shared.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AuthzManifestGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: AuthzManifestGenerator <basePackage> <outputFile>");
            System.exit(1);
        }

        String basePackage = args[0];
        String outputFile = args[1];

        System.out.println("Scanning for @PolicyResource in package: " + basePackage);

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(Scanners.TypesAnnotated));

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(PolicyResource.class);
        List<AuthzManifest.ResourceManifest> resources = new ArrayList<>();

        for (Class<?> clazz : annotatedClasses) {
            PolicyResource resourceAnno = clazz.getAnnotation(PolicyResource.class);
            List<AuthzManifest.FieldManifest> fields = new ArrayList<>();

            if (clazz.isRecord()) {
                for (RecordComponent rc : clazz.getRecordComponents()) {
                    PolicyField pf = rc.getAnnotation(PolicyField.class);
                    if (pf != null) {
                        fields.add(mapField(rc.getName(), pf));
                    }
                }
            } else {
                for (Field f : clazz.getDeclaredFields()) {
                    PolicyField pf = f.getAnnotation(PolicyField.class);
                    if (pf != null) {
                        fields.add(mapField(f.getName(), pf));
                    }
                }
            }

            resources.add(new AuthzManifest.ResourceManifest(
                    resourceAnno.namespace(),
                    resourceAnno.name(),
                    resourceAnno.action(),
                    resourceAnno.description(),
                    fields
            ));
        }

        AuthzManifest manifest = new AuthzManifest(resources);
        ObjectMapper mapper = new ObjectMapper();

        File out = new File(outputFile);
        out.getParentFile().mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, manifest);

        System.out.println("Successfully generated authz manifest at: " + out.getAbsolutePath());
    }

    private static AuthzManifest.FieldManifest mapField(String name, PolicyField pf) {
        List<String> allowedValues = pf.allowedValues().length > 0 ? Arrays.asList(pf.allowedValues()) : null;
        String endpoint = pf.optionsEndpoint().isBlank() ? null : pf.optionsEndpoint();
        return new AuthzManifest.FieldManifest(name, pf.displayName(), pf.type(), allowedValues, endpoint);
    }
}
