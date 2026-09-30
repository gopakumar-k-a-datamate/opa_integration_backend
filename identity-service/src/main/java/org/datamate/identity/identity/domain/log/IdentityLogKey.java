/*
 * Copyright (c) 2025 Datamate. All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Datamate ("Confidential Information").
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the
 * license agreement you entered into with Datamate.
 */

package org.datamate.identity.identity.domain.log;

import com.datamate.bedrock.framework.common.logging.schema.LogKey;

/**
 * Domain-specific structured logging keys for the Identity microservice.
 * Implements Bedrock's {@link LogKey} interface for compile-time safety and consistent indexing.
 */
public enum IdentityLogKey implements LogKey {

    TARGET_USERNAME("targetUsername"),
    TARGET_USER_ID("targetUserId"),
    EMAIL("email"),
    PHONE_NUMBER("phoneNumber"),
    ROLES_ASSIGNED("rolesAssigned"),
    ROLE_NAME("roleName"),
    ROLE_ID("roleId"),
    AUTH_SUCCESS("authSuccess"),
    CLIENT_IP("clientIp");

    private final String keyName;

    IdentityLogKey(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public String key() {
        return this.keyName;
    }
}
