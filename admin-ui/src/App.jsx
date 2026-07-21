import React, { useState } from 'react';
import RoleSelector from './components/RoleSelector';
import PolicyGrid from './components/PolicyGrid';
import './index.css';

function App() {
  const [role, setRole] = useState('ACCOUNTANT');
  const [moduleName, setModuleName] = useState('finance');

  return (
    <div className="app-container">
      <div className="header">
        <h1>Authorization Dashboard</h1>
        <RoleSelector role={role} setRole={setRole} />
      </div>

      <div className="glass-panel">
        <div className="tabs">
          <button 
            className={`tab ${moduleName === 'finance' ? 'active' : ''}`}
            onClick={() => setModuleName('finance')}
          >
            Finance Module
          </button>
          <button 
            className={`tab ${moduleName === 'clinical' ? 'active' : ''}`}
            onClick={() => setModuleName('clinical')}
          >
            Clinical Module
          </button>
        </div>

        <PolicyGrid role={role} moduleName={moduleName} />
      </div>
    </div>
  );
}

export default App;
