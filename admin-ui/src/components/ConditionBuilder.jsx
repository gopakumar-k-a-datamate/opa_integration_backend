import React, { useState, useEffect } from 'react';
import { fetchFields } from '../api/apiClient';

const ConditionBuilder = ({ permissionCode, existingExpression, onClose, onSave }) => {
  const [fields, setFields] = useState([]);
  const [operator, setOperator] = useState(existingExpression?.operator || 'AND');
  const [rules, setRules] = useState(existingExpression?.children || []);

  useEffect(() => {
    loadFields();
  }, [permissionCode]);

  const loadFields = async () => {
    const data = await fetchFields(permissionCode);
    setFields(data);
  };

  const addRule = () => {
    setRules([...rules, { field: fields[0]?.fieldName || '', comparison: '==', value: '' }]);
  };

  const updateRule = (index, key, val) => {
    const newRules = [...rules];
    newRules[index][key] = val;
    setRules(newRules);
  };

  const removeRule = (index) => {
    setRules(rules.filter((_, i) => i !== index));
  };

  const handleSave = () => {
    if (rules.length === 0) {
      onSave(permissionCode, null);
    } else {
      onSave(permissionCode, { operator, children: rules });
    }
  };

  return (
    <div className="modal-overlay">
      <div className="glass-panel modal-content">
        <div className="modal-header">
          <h3>Condition Builder — {permissionCode}</h3>
          <button className="btn" onClick={onClose}>✕</button>
        </div>
        
        <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span>IF</span>
          <select value={operator} onChange={e => setOperator(e.target.value)}>
            <option value="AND">ALL</option>
            <option value="OR">ANY</option>
          </select>
          <span>of the following:</span>
        </div>

        <div>
          {rules.map((rule, i) => (
            <div key={i} className="rule-row">
              <select value={rule.field} onChange={e => updateRule(i, 'field', e.target.value)}>
                {fields.map(f => (
                  <option key={f.fieldName} value={f.fieldName}>{f.displayName}</option>
                ))}
              </select>
              <select value={rule.comparison} onChange={e => updateRule(i, 'comparison', e.target.value)}>
                <option value="==">==</option>
                <option value="!=">!=</option>
                <option value="<=">&lt;=</option>
                <option value=">=">&gt;=</option>
              </select>
              <input 
                type="text" 
                placeholder="Value..." 
                value={rule.value} 
                onChange={e => updateRule(i, 'value', e.target.value)}
                style={{ flex: 1 }}
              />
              <button className="btn" style={{ padding: '0.5rem' }} onClick={() => removeRule(i)}>✕</button>
            </div>
          ))}
        </div>

        <div style={{ marginTop: '1rem' }}>
          <button className="btn" onClick={addRule}>+ Add Rule</button>
        </div>

        <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
          <button className="btn" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave}>Apply</button>
        </div>
      </div>
    </div>
  );
};

export default ConditionBuilder;
