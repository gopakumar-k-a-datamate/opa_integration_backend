package org.datamate.clinic.clinical.application.dto;

import org.datamate.authz.annotation.PolicyField;
import org.datamate.authz.annotation.PolicyResource;
import org.datamate.authz.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "clinical", resourceName = "encounter", action = "read", description = "View patient encounters")
public class ReadPatientEncounterPolicyResource {

    @PolicyField(type = FieldType.STRING, displayName = "Doctor Specialty", allowedValues = {"CARDIOLOGY", "NEUROLOGY", "GENERAL_PRACTICE", "PEDIATRICS"})
    private String specialty;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Is Confidential")
    private Boolean isConfidential;

    @PolicyField(type = FieldType.NUMBER, displayName = "Patient Age")
    private Integer patientAge;

    @PolicyField(type = FieldType.STRING, displayName = "Diagnosis Code")
    private String diagnosisCode;

    @PolicyField(type = FieldType.DATE, displayName = "Encounter Date")
    private String encounterDate;
    
    @PolicyField(type = FieldType.STRING, displayName = "Clinic Location", allowedValues = {"MAIN_CAMPUS", "NORTH_BRANCH", "SOUTH_BRANCH"})
    private String locationId;

}
