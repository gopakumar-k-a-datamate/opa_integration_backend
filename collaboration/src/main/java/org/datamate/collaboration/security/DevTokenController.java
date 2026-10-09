package org.datamate.collaboration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.Base64;

/**
 * DEVELOPMENT ONLY Utility.
 * Generates an RSA keypair on startup, registers it, and provides an endpoint 
 * to mint valid JWT tickets so the frontend can test the chat service locally.
 */
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final InMemoryKeyRegistry keyRegistry;
    private KeyPair keyPair;
    private final String DEV_KID = "dev-test-key-1";

    @PostConstruct
    public void initDevKeys() throws NoSuchAlgorithmException {
        // 1. Generate an RSA keypair just for local testing
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.keyPair = generator.generateKeyPair();

        // 2. Register the public key into the backend's KeyRegistry so it can verify the tokens
        String base64PubKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        keyRegistry.registerKey(DEV_KID, base64PubKey);
        System.out.println("DEV WARNING: Initialized local RSA keys for JWT testing.");
    }

    /**
     * Endpoint to generate a valid ticket for a specific user and thread.
     * Example: GET /api/v1/dev/token?userId=user123&threadId=thread456
     */
    @GetMapping("/token")
    public String generateTestToken(@RequestParam String userId, @RequestParam String threadId) {
        
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + (1000 * 60 * 60); // 1 hour expiration
        
        // Build the JWT identical to how the Upstream Auth server would
        return Jwts.builder()
                .header()
                   .keyId(DEV_KID)
                .and()
                .subject(userId)
                .claim("threadId", threadId)
                .issuedAt(new Date(nowMillis))
                .expiration(new Date(expMillis))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
