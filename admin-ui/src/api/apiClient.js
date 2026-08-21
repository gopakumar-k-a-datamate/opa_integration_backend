const getApiUrl = (identifier) => {
  // identifier can be a namespace (e.g. 'pharmacy') or a permissionCode (e.g. 'pharmacy:prescription:view')
  if (!identifier) return 'http://localhost:8083';
  
  if (identifier.startsWith('pharmacy')) {
    return 'http://localhost:8083'; // pharmacy-microservice
  }
  
  return 'http://localhost:8083'; // default fallback
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
      body: JSON.stringify({ subjectType, subjectId, namespace, policies })
    });
    if (!res.ok) {
      let errorData = { message: 'Failed to save policies' };
      try {
        errorData = await res.json();
        // Spring ProblemDetail uses 'detail', but UI expects 'message'
        if (errorData.detail && !errorData.message) {
          errorData.message = errorData.detail;
        }
      } catch (e) {
        console.warn('Could not parse error response');
      }
      const error = new Error('Failed to save');
      // Mock Axios-style error shape for PolicyGrid.jsx
      error.response = { data: errorData };
      throw error;
    }
    return await res.json();
  } catch (err) {
    console.error(`Backend ${baseUrl} unavailable:`, err);
    throw err;
  }
};

export const fetchRoles = async () => {
  // Identity Service runs on port 8085
  const baseUrl = 'http://localhost:8085';
  try {
    const res = await fetch(`${baseUrl}/api/v1/roles`);
    if (!res.ok) throw new Error('Failed to fetch roles');
    const data = await res.json();
    return data.content || data; // Handle Paged<RoleDto> format
  } catch (err) {
    console.error(`Identity Service ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchUsers = async () => {
  const baseUrl = 'http://localhost:8085';
  try {
    const res = await fetch(`${baseUrl}/api/v1/users`);
    if (!res.ok) throw new Error('Failed to fetch users');
    const data = await res.json();
    return data.content || data; // Handle Paged<UserResponseDto> format
  } catch (err) {
    console.error(`Identity Service ${baseUrl} unavailable:`, err);
    throw new Error('Not available');
  }
};

export const fetchNamespaces = async (microservicePort) => {
  // microservicePort would be 8083 for Pharmacy
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

export const fetchOptionsEndpoint = async (permissionCode, endpoint, page, search) => {
  const baseUrl = getApiUrl(permissionCode);
  const queryParams = new URLSearchParams();
  queryParams.append('page', page);
  queryParams.append('size', 20);
  if (search) {
    queryParams.append('search', search);
  }
  
  try {
    const res = await fetch(`${baseUrl}${endpoint}?${queryParams.toString()}`);
    if (!res.ok) throw new Error('Failed to fetch options');
    return await res.json();
  } catch (err) {
    console.error(`Endpoint ${baseUrl}${endpoint} unavailable:`, err);
    return { content: [], last: true, page: 0 };
  }
};

