const getApiUrl = (identifier) => {
  // identifier can be a namespace (e.g. 'clinical') or a permissionCode (e.g. 'clinical:appointment:view')
  if (identifier && (identifier === 'clinical' || identifier.startsWith('clinical:'))) {
    return 'http://localhost:8082';
  }
  return 'http://localhost:8081';
};

export const fetchPolicies = async (subjectType, subjectId, namespace) => {
  const baseUrl = getApiUrl(namespace);
  try {
    const res = await fetch(`${baseUrl}/internal/authz/policies?subjectType=${subjectType}&subjectId=${subjectId}&namespace=${namespace}`);
    if (!res.ok) throw new Error('Failed to fetch');
    return await res.json();
  } catch (err) {
    console.warn(`Backend ${baseUrl} unavailable, using mock policies.`);
    return [
      { permissionCode: `${namespace}:journal:create`, action: 'create', namespace: namespace, resourceName: 'journal', policyId: 1, effect: 'ALLOW', expressionJson: null, enabled: true, disabledReason: null },
      { permissionCode: `${namespace}:journal:view`, action: 'view', namespace: namespace, resourceName: 'journal', policyId: 2, effect: 'ALLOW', expressionJson: null, enabled: true, disabledReason: null },
      { permissionCode: `${namespace}:journal:delete`, action: 'delete', namespace: namespace, resourceName: 'journal', policyId: null, effect: null, expressionJson: null, enabled: false, disabledReason: null },
      { permissionCode: `${namespace}:report:view`, action: 'view', namespace: namespace, resourceName: 'report', policyId: 3, effect: 'ALLOW', expressionJson: null, enabled: true, disabledReason: null },
      { permissionCode: `${namespace}:report:export`, action: 'export', namespace: namespace, resourceName: 'report', policyId: null, effect: null, expressionJson: null, enabled: false, disabledReason: null }
    ];
  }
};

export const fetchFields = async (permissionCode) => {
  const baseUrl = getApiUrl(permissionCode);
  try {
    const res = await fetch(`${baseUrl}/internal/authz/permissions/${permissionCode}/fields`);
    if (!res.ok) throw new Error('Failed to fetch');
    return await res.json();
  } catch (err) {
    console.warn(`Backend ${baseUrl} unavailable, using mock fields.`);
    return [
      { fieldName: 'amount', fieldType: 'NUMBER', displayName: 'Amount' },
      { fieldName: 'departmentId', fieldType: 'STRING', displayName: 'Department ID' }
    ];
  }
};

export const savePolicies = async (subjectType, subjectId, namespace, policies) => {
  const baseUrl = getApiUrl(namespace);
  try {
    const res = await fetch(`${baseUrl}/internal/authz/policies`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      // Note: Admin UI workflow payload might just require the array, or a wrapper object.
      // Based on 06-api-endpoints.md, it expects a wrapper with subjectType, subjectId, and policies.
      body: JSON.stringify({ subjectType, subjectId, namespace, policies })
    });
    if (!res.ok) throw new Error('Failed to save');
    return await res.json();
  } catch (err) {
    console.warn(`Backend ${baseUrl} unavailable, mocking save success.`);
    return { message: "Policies updated successfully (MOCK)." };
  }
};
