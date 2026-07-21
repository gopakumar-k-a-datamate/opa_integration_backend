import React, { useState, useEffect } from 'react';
import { fetchPolicies, savePolicies } from '../api/apiClient';
import ConditionBuilder from './ConditionBuilder';

const PolicyGrid = ({ role, moduleName }) => {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [activeConditionPermission, setActiveConditionPermission] = useState(null); // The permissionCode being edited

  useEffect(() => {
    loadPolicies();
  }, [role, moduleName]);

  const loadPolicies = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchPolicies('ROLE', role, moduleName);
      setPolicies(data);
    } catch (err) {
      setError('Service Not Available');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (permissionCode) => {
    setPolicies(prev => prev.map(p => {
      if (p.permissionCode === permissionCode) {
        const newEnabled = !p.enabled;
        return { 
          ...p, 
          enabled: newEnabled,
          effect: (newEnabled && !p.effect) ? 'ALLOW' : p.effect 
        };
      }
      return p;
    }));
  };

  const handleSave = async () => {
    await savePolicies('ROLE', role, moduleName, policies);
    alert('Policies updated successfully.');
  };

  const handleConditionsSaved = (permissionCode, newExpression) => {
    setPolicies(prev => prev.map(p => 
      p.permissionCode === permissionCode 
        ? { ...p, expressionJson: newExpression, enabled: true, effect: p.effect || 'ALLOW' } 
        : p
    ));
    setActiveConditionPermission(null);
  };

  if (loading) return <div>Loading policies...</div>;
  if (error) return (
    <div className="glass-panel" style={{ padding: '2rem', color: '#fca5a5', textAlign: 'center', marginTop: '1rem' }}>
      ⚠️ {error} - The backend for {moduleName} is currently offline.
    </div>
  );

  // Group by resourceName
  const grouped = policies.reduce((acc, p) => {
    acc[p.resourceName] = acc[p.resourceName] || [];
    acc[p.resourceName].push(p);
    return acc;
  }, {});

  return (
    <div>
      {Object.keys(grouped).map(resource => (
        <div key={resource} className="resource-group">
          <div className="resource-header">
            📁 {resource}
          </div>
          <div className="glass-panel" style={{ padding: '0.5rem' }}>
            {grouped[resource].map(p => (
              <div key={p.permissionCode} className="policy-card">
                <div className="policy-info">
                  <input 
                    type="checkbox" 
                    className="checkbox"
                    checked={p.enabled}
                    onChange={() => handleToggle(p.permissionCode)}
                  />
                  <span style={{ fontWeight: 500, minWidth: '80px' }}>{p.action}</span>
                  {p.expressionJson && (
                    <span className="badge">Has Conditions</span>
                  )}
                  {p.disabledReason && (
                    <span className="badge" style={{ background: 'rgba(239, 68, 68, 0.2)', color: '#fca5a5' }}>
                      ⚠️ {p.disabledReason}
                    </span>
                  )}
                </div>
                <div className="actions">
                  <button className="btn" onClick={() => setActiveConditionPermission(p.permissionCode)}>
                    ⚙️
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
      
      <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'flex-end' }}>
        <button className="btn btn-primary" onClick={handleSave}>
          💾 Save Changes
        </button>
      </div>

      {activeConditionPermission && (
        <ConditionBuilder 
          permissionCode={activeConditionPermission}
          existingExpression={policies.find(p => p.permissionCode === activeConditionPermission)?.expressionJson}
          onClose={() => setActiveConditionPermission(null)}
          onSave={handleConditionsSaved}
        />
      )}
    </div>
  );
};

export default PolicyGrid;
