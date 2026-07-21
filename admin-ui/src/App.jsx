import React, { useState, useEffect } from 'react';
import RoleSelector from './components/RoleSelector';
import PolicyGrid from './components/PolicyGrid';
import { fetchNamespaces } from './api/apiClient';
import './index.css';

function App() {
  const [role, setRole] = useState('ACCOUNTANT');
  const [moduleName, setModuleName] = useState('finance');
  const [availableModules, setAvailableModules] = useState(['finance', 'clinical']);

  useEffect(() => {
    const loadModules = async () => {
      try {
        // Fetch from both microservices (falling back to empty arrays if offline)
        const financeModules = await fetchNamespaces(8081).catch(() => []);
        const clinicModules = await fetchNamespaces(8082).catch(() => []);
        
        const combined = [...new Set([...financeModules, ...clinicModules])];
        if (combined.length > 0) {
          setAvailableModules(combined);
        }
      } catch (err) {
        console.error("Failed to load namespaces");
      }
    };
    loadModules();
  }, []);

  return (
    <div className="app-container">
      <div className="header">
        <h1>Authorization Dashboard</h1>
        <RoleSelector role={role} setRole={setRole} />
      </div>

      <div className="glass-panel">
        <div className="tabs">
          {availableModules.map(mod => (
            <button 
              key={mod}
              className={`tab ${moduleName === mod ? 'active' : ''}`}
              onClick={() => setModuleName(mod)}
              style={{ textTransform: 'capitalize' }}
            >
              {mod} Module
            </button>
          ))}
        </div>

        <PolicyGrid role={role} moduleName={moduleName} />
      </div>
    </div>
  );
}

export default App;
