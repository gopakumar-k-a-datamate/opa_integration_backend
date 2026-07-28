package org.datamate.clinic.billing.application.dto;


import lombok.Getter;
import lombok.Setter;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;

@Getter
@Setter
@PolicyResource(namespace = "billing", name = "invoice", action = "update", description = "Update patient invoice")
public class UpdateInvoicePolicyResource {

    @PolicyField(type = FieldType.NUMBER, displayName = "Total Amount")
    private Double totalAmount;

    @PolicyField(type = FieldType.BOOLEAN, displayName = "Is Fully Paid")
    private Boolean isPaid;
}
