package org.datamate.authz.rest.controller;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.service.policy.GetConditionFieldsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * Admin UI — Condition Builder API.
 *
 * <p>{@code GET /internal/authz/permissions/{permissionCode}/fields}</p>
 *
 * <p>Returns ACTIVE condition fields for a permission code.
 * Used by the Condition Builder UI to populate the field and operator dropdowns.</p>
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnProperty(name = "datamate.authz.admin.enabled", havingValue = "true")
@RequestMapping("/internal/authz/permissions")
public class ConditionFieldController {

    private final GetConditionFieldsService GetConditionFieldsService;
/**
     * @param permissionCode e.g. {@code finance:journal:create}
     */
    @GetMapping("/{permissionCode}/fields")
    public ResponseEntity<List<ConditionFieldDto>> getFields(
            @PathVariable String permissionCode) {
        return ResponseEntity.ok(GetConditionFieldsService.getFields(permissionCode));
    }
}




