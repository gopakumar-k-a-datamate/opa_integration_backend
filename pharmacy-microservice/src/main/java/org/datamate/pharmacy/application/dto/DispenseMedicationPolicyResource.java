package org.datamate.pharmacy.application.dto;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "pharmacy", resourceName = "medication", action = "dispense", description = "Dispense a medication to a patient")
public class DispenseMedicationPolicyResource {

    @PolicyField(type = FieldType.STRING, displayName = "Drug Class", allowedValues = {"OTC", "PRESCRIPTION", "CONTROLLED"})
    private String drugClass;

    @PolicyField(type = FieldType.NUMBER, displayName = "Patient Age")
    private Integer patientAge;

    public String getDrugClass() {
        return drugClass;
    }

    public void setDrugClass(String drugClass) {
        this.drugClass = drugClass;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }
}
