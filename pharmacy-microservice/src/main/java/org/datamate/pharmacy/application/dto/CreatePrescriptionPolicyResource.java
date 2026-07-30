package org.datamate.pharmacy.application.dto;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;

@PolicyResource(namespace = "pharmacy", name = "prescription", action = "create", description = "Create a prescription")
public class CreatePrescriptionPolicyResource {

    @PolicyField(type = FieldType.STRING, displayName = "Doctor Level", allowedValues = {"MAIN", "SENIOR", "JUNIOR"})
    private String doctorLevel;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Is Same Ward", allowedValues = {"true", "false"})
    private boolean isSameWard;

    public String getDoctorLevel() {
        return doctorLevel;
    }

    public void setDoctorLevel(String doctorLevel) {
        this.doctorLevel = doctorLevel;
    }

    public boolean isSameWard() {
        return isSameWard;
    }

    public void setSameWard(boolean sameWard) {
        isSameWard = sameWard;
    }
}
