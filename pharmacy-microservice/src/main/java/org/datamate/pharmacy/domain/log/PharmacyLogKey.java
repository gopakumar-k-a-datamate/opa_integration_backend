/*
 * Copyright (c) 2025 Datamate. All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Datamate ("Confidential Information").
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the
 * license agreement you entered into with Datamate.
 */

package org.datamate.pharmacy.domain.log;

import com.datamate.bedrock.framework.common.logging.schema.LogKey;

/**
 * Domain-specific structured logging keys for the Pharmacy microservice.
 * Implements Bedrock's {@link LogKey} interface for compile-time safety and consistent indexing.
 */
public enum PharmacyLogKey implements LogKey {

    MEDICATION_ID("medicationId"),
    MEDICATION_NAME("medicationName"),
    PATIENT_ID("patientId"),
    PATIENT_NAME("patientName"),
    PATIENT_AGE("patientAge"),
    QUANTITY("quantity"),
    DRUG_CLASS("drugClass"),
    PREVIOUS_STOCK("previousStock"),
    REMAINING_STOCK("remainingStock"),
    MINIMUM_STOCK_THRESHOLD("minimumStockThreshold"),
    PRESCRIPTION_ID("prescriptionId"),
    PRACTITIONER_ID("practitionerId"),
    ALERT_TRIGGERED("alertTriggered");

    private final String keyName;

    PharmacyLogKey(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public String key() {
        return this.keyName;
    }
}
