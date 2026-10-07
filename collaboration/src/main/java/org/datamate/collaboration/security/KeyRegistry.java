package org.datamate.collaboration.security;

import java.security.PublicKey;
import java.util.Optional;

/**
 * Strategy interface for resolving public keys used to verify JWT signatures.
 */
public interface KeyRegistry {
    
    /**
     * Retrieves the public key associated with the given Key ID (kid).
     * 
     * @param kid The key identifier.
     * @return An Optional containing the PublicKey if found, empty otherwise.
     */
    Optional<PublicKey> getKey(String kid);
}
