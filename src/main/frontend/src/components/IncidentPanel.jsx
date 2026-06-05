import React from 'react';
import {
  formatTimeAgo,
  getIncidentCardTitle,
  getIncidentKey,
  getIncidentLocationLabel,
  getIncidentSeverity,
  getIncidentTypeLabel,
} from '../utils/incidents.js';

export function IncidentPanel({ incidents, status, error, selectedIncident, onSelectIncident }) {
  return (
    <aside className="incident-panel" aria-live="polite">
      <div className="section-heading">
        <p className="eyebrow">Live feed</p>
        <h2>Recent incidents</h2>
      </div>
      <p className="panel-meta">{incidents.length} campus alert{incidents.length === 1 ? '' : 's'} shown</p>
      {status === 'loading' && <p>Loading incidents...</p>}
      {status === 'error' && <p className="error-text">Unable to load incidents: {error}</p>}
      {status === 'empty' && <p>No incidents found.</p>}
      {status === 'ready' && (
        <div className="incident-list">
          {incidents.map((incident) => (
            <button
              className={
                selectedIncident && selectedIncident.id === incident.id
                  ? 'incident-item selected'
                  : 'incident-item'
              }
              key={getIncidentKey(incident)}
              type="button"
              onClick={() => onSelectIncident(incident)}
            >
              <span className="incident-title">{getIncidentCardTitle(incident)}</span>
              <span className="incident-meta">{formatTimeAgo(incident)}</span>
              <span className="incident-meta">{getIncidentLocationLabel(incident)}</span>
              <span className={`incident-risk incident-risk-${incident.alertStatus || 'active'}`}>
                {incident.alertStatus || getIncidentSeverity(incident)}
              </span>
            </button>
          ))}
        </div>
      )}
    </aside>
  );
}
