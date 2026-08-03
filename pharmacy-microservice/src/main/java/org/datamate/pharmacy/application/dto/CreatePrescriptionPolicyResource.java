package org.datamate.pharmacy.application.dto;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;

@PolicyResource(namespace = "pharmacy", resourceName = "prescription", action = "create", description = "Create Prescription")
public class CreatePrescriptionPolicyResource {

    @PolicyField(type = FieldType.STRING, displayName = "Doctor Level", allowedValues = {"MAIN","JUNIOR","SENIOR"})
    private String doctorLevel;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Same Ward")
    private Boolean isSameWard;

    public void setDoctorLevel(String doctorLevel) {this.doctorLevel =doctorLevel;}
    public void setIsSameWard(Boolean isSameWard) {this.isSameWard = isSameWard;}
}
