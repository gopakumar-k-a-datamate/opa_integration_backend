import React, { useState, useEffect } from 'react';
import { fetchFields } from '../api/apiClient';

const ConditionRule = ({ rule, fields, onChange, onRemove }) => {
  const selectedField = fields.find(f => f.fieldName === rule.field);

  const renderValueInput = () => {
    if (!selectedField) {
      return (
        <input 
          type="text" 
          placeholder="Value..." 
          value={rule.value || ''} 
          onChange={e => onChange({ ...rule, value: e.target.value })}
          style={{ flex: 1, padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
        />
      );
    }

    if (selectedField.allowedValues && selectedField.allowedValues.length > 0) {
      return (
        <select 
          value={rule.value || ''} 
          onChange={e => onChange({ ...rule, value: e.target.value })}
          style={{ flex: 1, padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
        >
          <option value="">Select value...</option>
          {selectedField.allowedValues.map(val => (
            <option key={val} value={val}>{val}</option>
          ))}
        </select>
      );
    }

    if (selectedField.fieldType === 'BOOLEAN') {
      return (
        <select 
          value={rule.value !== undefined ? String(rule.value) : ''} 
          onChange={e => onChange({ ...rule, value: e.target.value === 'true' })}
          style={{ flex: 1, padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
        >
          <option value="">Select...</option>
          <option value="true">True</option>
          <option value="false">False</option>
        </select>
      );
    }

    if (selectedField.fieldType === 'NUMBER') {
      return (
        <input 
          type="number" 
          placeholder="Value..." 
          value={rule.value || ''} 
          onChange={e => onChange({ ...rule, value: Number(e.target.value) })}
          style={{ flex: 1, padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
        />
      );
    }

    return (
      <input 
        type="text" 
        placeholder="Value..." 
        value={rule.value || ''} 
        onChange={e => onChange({ ...rule, value: e.target.value })}
        style={{ flex: 1, padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
      />
    );
  };

  const handleFieldChange = (e) => {
    const newFieldName = e.target.value;
    const newField = fields.find(f => f.fieldName === newFieldName);
    let defaultValue = '';
    if (newField?.fieldType === 'BOOLEAN') defaultValue = true;
    else if (newField?.allowedValues?.length > 0) defaultValue = newField.allowedValues[0];

    onChange({ ...rule, field: newFieldName, value: defaultValue });
  };

  return (
    <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', width: '100%', alignItems: 'center' }}>
      <select value={rule.field || ''} onChange={handleFieldChange} style={{ padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px', minWidth: '150px' }}>
        {fields.map(f => (
          <option key={f.fieldName} value={f.fieldName}>{f.displayName}</option>
        ))}
      </select>
      <select value={rule.comparison || '=='} onChange={e => onChange({ ...rule, comparison: e.target.value })} style={{ padding: '0.4rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
        <option value="==">==</option>
        <option value="!=">!=</option>
        <option value="<=">&lt;=</option>
        <option value=">=">&gt;=</option>
        <option value="<">&lt;</option>
        <option value=">">&gt;</option>
      </select>
      {renderValueInput()}
      <button className="btn" style={{ padding: '0.4rem 0.6rem', background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5' }} onClick={onRemove}>✕</button>
    </div>
  );
};

const ConditionGroup = ({ node, fields, onChange, onRemove, isRoot }) => {
  const handleOperatorChange = (e) => {
    onChange({ ...node, operator: e.target.value });
  };

  const handleChildChange = (index, newChild) => {
    const newChildren = [...(node.children || [])];
    newChildren[index] = newChild;
    onChange({ ...node, children: newChildren });
  };

  const handleRemoveChild = (index) => {
    const newChildren = (node.children || []).filter((_, i) => i !== index);
    onChange({ ...node, children: newChildren });
  };

  const addRule = () => {
    const defaultField = fields[0];
    let defaultValue = '';
    if (defaultField?.fieldType === 'BOOLEAN') defaultValue = true;
    else if (defaultField?.allowedValues?.length > 0) defaultValue = defaultField.allowedValues[0];

    const newRule = { 
      field: defaultField?.fieldName || '', 
      comparison: '==', 
      value: defaultValue 
    };
    const newChildren = [...(node.children || []), newRule];
    onChange({ ...node, children: newChildren });
  };

  const addGroup = () => {
    const newGroup = { operator: 'AND', children: [] };
    const newChildren = [...(node.children || []), newGroup];
    onChange({ ...node, children: newChildren });
  };

  const children = node.children || [];
  // Separate rules and groups to cluster rules together at the top of the group card
  const rules = children.map((c, i) => ({ child: c, index: i })).filter(item => !item.child.operator);
  const groups = children.map((c, i) => ({ child: c, index: i })).filter(item => item.child.operator);

  return (
    <div style={{ 
      border: '1px solid var(--border-color)', 
      borderRadius: '8px', 
      padding: '1.5rem', 
      background: 'rgba(255, 255, 255, 0.02)',
      position: 'relative',
      marginBottom: isRoot ? '0' : '1rem',
      marginTop: isRoot ? '0' : '1rem'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
        <span style={{ fontWeight: 500, color: 'var(--text-primary)' }}>Match</span>
        <select 
          value={node.operator || 'AND'} 
          onChange={handleOperatorChange} 
          style={{ 
            padding: '0.3rem', 
            fontSize: '0.9rem', 
            border: '1px solid var(--border-color)',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          <option value="AND">ALL</option>
          <option value="OR">ANY</option>
        </select>
        {!isRoot && (
          <button className="btn" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', marginLeft: 'auto', background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5' }} onClick={onRemove}>
            🗑 Remove Group
          </button>
        )}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem', marginBottom: groups.length > 0 ? '1.5rem' : '0' }}>
        {rules.map((item) => (
          <ConditionRule 
            key={`rule-${item.index}`}
            rule={item.child} 
            fields={fields} 
            onChange={(newRule) => handleChildChange(item.index, newRule)} 
            onRemove={() => handleRemoveChild(item.index)} 
          />
        ))}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        {groups.map((item, groupIdx) => {
          return (
            <React.Fragment key={`group-${item.index}`}>
              {groupIdx > 0 && (
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  margin: '1rem 0'
                }}>
                  <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }}></div>
                  <div style={{ 
                    padding: '0 1rem', 
                    color: 'var(--text-secondary)', 
                    fontWeight: 'bold', 
                    fontSize: '0.85rem',
                    letterSpacing: '1px'
                  }}>
                    {node.operator}
                  </div>
                  <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }}></div>
                </div>
              )}
              <ConditionGroup 
                node={item.child} 
                fields={fields} 
                onChange={(newChild) => handleChildChange(item.index, newChild)} 
                onRemove={() => handleRemoveChild(item.index)} 
                isRoot={false}
              />
            </React.Fragment>
          );
        })}
      </div>

      <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.5rem' }}>
        <button className="btn" style={{ fontSize: '0.8rem', padding: '0.5rem 1rem', background: 'rgba(59, 130, 246, 0.1)', color: '#60a5fa' }} onClick={addRule}>+ Add Rule</button>
        <button className="btn" style={{ fontSize: '0.8rem', padding: '0.5rem 1rem', background: 'rgba(59, 130, 246, 0.1)', color: '#60a5fa' }} onClick={addGroup}>+ Add Group</button>
      </div>
    </div>
  );
};

const generatePreview = (node, fields) => {
  if (!node) return '';
  
  if (node.operator) {
    if (!node.children || node.children.length === 0) return '(Empty Group)';
    const childPreviews = node.children.map(c => generatePreview(c, fields)).filter(Boolean);
    if (childPreviews.length === 0) return '(Empty Group)';
    if (childPreviews.length === 1) return childPreviews[0];
    return `(${childPreviews.join(` ${node.operator} `)})`;
  }
  
  // It's a rule
  const fieldDisplay = fields.find(f => f.fieldName === node.field)?.displayName || node.field || 'Unknown Field';
  const val = typeof node.value === 'string' ? `"${node.value}"` : node.value;
  return `${fieldDisplay} ${node.comparison || '=='} ${val}`;
};

const ConditionBuilder = ({ permissionCode, existingExpression, onClose, onSave }) => {
  const [fields, setFields] = useState([]);
  
  const [expressionTree, setExpressionTree] = useState(() => {
    if (existingExpression?.operator) {
      return existingExpression;
    }
    return { operator: 'AND', children: existingExpression?.children || [] };
  });

  useEffect(() => {
    loadFields();
  }, [permissionCode]);

  const loadFields = async () => {
    const data = await fetchFields(permissionCode);
    setFields(data);
  };

  const handleSave = () => {
    if (!expressionTree.children || expressionTree.children.length === 0) {
      onSave(permissionCode, null);
    } else {
      onSave(permissionCode, expressionTree);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="glass-panel modal-content" style={{ margin: 'auto', maxWidth: '900px', width: '90%', maxHeight: '90vh', overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
        <div className="modal-header">
          <h3>Condition Builder</h3>
          <button className="btn" onClick={onClose}>✕</button>
        </div>
        
        <div style={{ flex: 1, overflowY: 'auto', paddingBottom: '1rem' }}>
          {fields.length > 0 ? (
            <ConditionGroup 
              node={expressionTree} 
              fields={fields} 
              onChange={setExpressionTree} 
              isRoot={true} 
            />
          ) : (
            <div>Loading fields...</div>
          )}
        </div>

        <div style={{ 
          marginTop: '1.5rem', 
          padding: '1.5rem', 
          background: 'rgba(0, 0, 0, 0.2)', 
          borderRadius: '8px',
          border: '1px solid var(--border-color)'
        }}>
          <h4 style={{ margin: '0 0 1rem 0', color: 'var(--text-secondary)', fontSize: '0.9rem', textTransform: 'uppercase', letterSpacing: '1px' }}>Preview</h4>
          <div style={{ 
            fontFamily: 'monospace', 
            color: 'var(--text-primary)', 
            fontSize: '0.9rem', 
            lineHeight: '1.5',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word'
          }}>
            {fields.length > 0 ? generatePreview(expressionTree, fields) : 'Loading preview...'}
          </div>
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
