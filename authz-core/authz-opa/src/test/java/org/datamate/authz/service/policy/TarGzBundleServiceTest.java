package org.datamate.authz.service.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TarGzBundleServiceTest {

    @Mock
    private com.datamate.bedrock.framework.common.logging.service.Logger log;

    @InjectMocks
    private TarGzBundleService service;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field logField = TarGzBundleService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(service, log);
    }

    @Test
    void testBuildBundle_generatesValidGzip() throws IOException {
        String namespace = "finance";
        String regoContent = "package authz.finance\n\ndefault allow = false";

        byte[] result = service.build(namespace, regoContent);

        assertNotNull(result);
        assertTrue(result.length > 0);

        // Check GZIP magic bytes (1F 8B)
        assertEquals((byte) 0x1F, result[0]);
        assertEquals((byte) 0x8B, result[1]);

        // Attempt to decompress to verify it's a valid GZIP payload
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(result))) {
            byte[] decompressed = gis.readAllBytes();
            assertTrue(decompressed.length > 0);
            
            String tarString = new String(decompressed, StandardCharsets.UTF_8);
            
            // The tar file should contain our filenames and content
            assertTrue(tarString.contains("authz/policy.rego"));
            assertTrue(tarString.contains(regoContent));
            assertTrue(tarString.contains(".manifest"));
            assertTrue(tarString.contains("{\"roots\": [\"app/authz/finance\"]}"));
            assertTrue(tarString.contains("ustar ")); // Magic tar header
        }
    }
}
