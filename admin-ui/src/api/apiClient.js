const getApiUrl = (identifier) => {
  // identifier can be a namespace (e.g. 'clinical') or a permissionCode (e.g. 'clinical:appointment:view')
  if (!identifier) return 'http://localhost:8081';
  
  if (identifier.startsWith('clinical') || identifier.startsWith('billing')) {
    return 'http://localhost:8082'; // clinic-modulith handles BOTH clinical and billing
  }
  if (identifier.startsWith('finance')) {
    return 'http://localhost:8081'; // finance-microservice
  }
  
  return 'http://localhost:8081'; // default fallback
};

export const fetchPolicies = async (subjectType, subjectId, namespace) => {
  const baseUrl = getApiUrl(namespace);
  try {
    const res = await fetch(`${baseUrl}/internal/authz/policies?subjectType=${subjectType}&subjectId=${subjectId}&namespace=${namespace}`);
    if (!res.ok) throw new Error('Failed to fetch');
    return await res.json();
  } catch (err) {
    console.error(`Backend ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchFields = async (permissionCode) => {
  const baseUrl = getApiUrl(permissionCode);
  try {
    const res = await fetch(`${baseUrl}/internal/authz/permissions/${permissionCode}/fields`);
    if (!res.ok) throw new Error('Failed to fetch');
    return await res.json();
  } catch (err) {
    console.error(`Backend ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
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
    console.error(`Backend ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchRoles = async () => {
  // Identity Service runs on port 8080
  const baseUrl = 'http://localhost:8080';
  try {
    const res = await fetch(`${baseUrl}/api/v1/roles`);
    if (!res.ok) throw new Error('Failed to fetch roles');
    return await res.json();
  } catch (err) {
    console.error(`Identity Service ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchUsers = async () => {
  const baseUrl = 'http://localhost:8080';
  try {
    const res = await fetch(`${baseUrl}/api/v1/users`);
    if (!res.ok) throw new Error('Failed to fetch users');
    return await res.json();
  } catch (err) {
    console.error(`Identity Service ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchNamespaces = async (microservicePort) => {
  // microservicePort would be 8081 for Finance or 8082 for Clinic
  const baseUrl = `http://localhost:${microservicePort}`;
  try {
    const res = await fetch(`${baseUrl}/internal/authz/namespaces`);
    if (!res.ok) throw new Error('Failed to fetch namespaces');
    return await res.json();
  } catch (err) {
    console.error(`Microservice ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};
