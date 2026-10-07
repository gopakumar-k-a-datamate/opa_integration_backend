package org.datamate.collaboration.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of KeyRegistry.
 * In a production microservice environment, this might be replaced by a JWKS-fetching implementation.
 */
@Slf4j
@Component
public class InMemoryKeyRegistry implements KeyRegistry {

    private final Map<String, PublicKey> keys = new ConcurrentHashMap<>();

    @Override
    public Optional<PublicKey> getKey(String kid) {
        return Optional.ofNullable(keys.get(kid));
    }

    /**
     * Registers a base64 encoded RSA public key.
     * 
     * @param kid Key ID
     * @param base64PublicKey PEM or Base64 encoded public key
     */
    public void registerKey(String kid, String base64PublicKey) {
        try {
            String pem = base64PublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] encoded = Base64.getDecoder().decode(pem);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            
            keys.put(kid, pubKey);
            log.info("Successfully registered public key for kid: {}", kid);
        } catch (Exception e) {
            log.error("Failed to load public key for kid: {}", kid, e);
            throw new IllegalArgumentException("Invalid public key format for kid: " + kid, e);
        }
    }
}
