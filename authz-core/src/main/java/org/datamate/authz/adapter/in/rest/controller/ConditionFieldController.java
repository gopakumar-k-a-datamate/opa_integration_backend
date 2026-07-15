package org.datamate.authz.adapter.in.rest.controller;

import org.datamate.authz.application.dto.policy.ConditionFieldDto;
import org.datamate.authz.application.port.in.policy.GetConditionFieldsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin UI — Condition Builder API.
 *
 * <p>{@code GET /internal/authz/permissions/{permissionCode}/fields}</p>
 *
 * <p>Returns ACTIVE condition fields for a permission code.
 * Used by the Condition Builder UI to populate the field and operator dropdowns.</p>
 */
@RestController
@RequestMapping("/internal/authz/permissions")
public class ConditionFieldController {

    private final GetConditionFieldsUseCase getConditionFieldsUseCase;

    public ConditionFieldController(GetConditionFieldsUseCase getConditionFieldsUseCase) {
        this.getConditionFieldsUseCase = getConditionFieldsUseCase;
    }

    /**
     * @param permissionCode e.g. {@code finance:journal:create}
     */
    @GetMapping("/{permissionCode}/fields")
    public ResponseEntity<List<ConditionFieldDto>> getFields(
            @PathVariable String permissionCode) {
        return ResponseEntity.ok(getConditionFieldsUseCase.getFields(permissionCode));
    }
}


