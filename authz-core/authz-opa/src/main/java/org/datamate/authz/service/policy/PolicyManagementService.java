package org.datamate.authz.service.policy;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.PolicySearchCriteria;
import org.datamate.authz.dto.policy.SubjectDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.rest.dto.SavePoliciesRequest;

import java.util.List;

/**
 * Core interface for managing policies, permissions, and related metadata.
 * Unifies the distinct use cases into a cohesive N-Tier service interface.
 */
public interface PolicyManagementService {

    /**
     * Get all active condition fields for a given permission code.
     */
    List<ConditionFieldDto> getConditionFields(String permissionCode);

    /**
     * Get all available namespaces.
     */
    List<String> getNamespaces();

    /**
     * Get all policies (as grid items) for a given subject and namespace.
     */
    List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace);

    /**
     * Get searchable policies for a given search criteria.
     */
    List<PolicyGridItemDto> getPolicies(PolicySearchCriteria criteria);

    /**
     * Get paginated & searchable policies using PolicySearchCriteria and Bedrock PageQuery.
     */
    Paged<PolicyGridItemDto> getPolicies(
            PolicySearchCriteria criteria,
            PageQuery pageQuery);

    /**
     * Get paginated & searchable policies for a given subject and namespace using Bedrock PageQuery.
     */
    Paged<PolicyGridItemDto> getPolicies(
            SubjectType subjectType, String subjectId, String namespace, String search,
            PageQuery pageQuery);

    /**
     * Search and paginate distinct active subjects using Bedrock PageQuery.
     */
    Paged<SubjectDto> getSubjects(
            SubjectType subjectType, String search,
            PageQuery pageQuery);

    /**
     * Save policies (state sync) for a given subject and namespace.
     */
    void savePolicies(SavePoliciesRequest request);
}
