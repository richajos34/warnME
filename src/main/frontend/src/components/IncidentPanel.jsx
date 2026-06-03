import React from 'react';

function getIncidentKey(incident) {
  return incident.id || `${incident.title}-${incident.timestamp}`;
}

export function IncidentPanel({ incidents, status, error, selectedIncident, onSelectIncident }) {
  return React.createElement(
    'aside',
    { className: 'incident-panel', 'aria-live': 'polite' },
    React.createElement('h2', null, 'Incidents'),
    React.createElement('p', { className: 'panel-meta' }, `${incidents.length} shown`),
    status === 'loading' && React.createElement('p', null, 'Loading incidents...'),
    status === 'error' &&
      React.createElement('p', { className: 'error-text' }, `Unable to load incidents: ${error}`),
    status === 'empty' && React.createElement('p', null, 'No incidents found.'),
    status === 'ready' &&
      React.createElement(
        'div',
        { className: 'incident-list' },
        incidents.map((incident) =>
          React.createElement(
            'button',
            {
              className:
                selectedIncident && selectedIncident.id === incident.id
                  ? 'incident-item selected'
                  : 'incident-item',
              key: getIncidentKey(incident),
              type: 'button',
              onClick: () => onSelectIncident(incident),
            },
            React.createElement('span', { className: 'incident-title' }, incident.title || 'Untitled incident'),
            React.createElement(
              'span',
              { className: 'incident-meta' },
              `${incident.status || 'Unknown'} · ${incident.type || 'Uncategorized'}`
            )
          )
        )
      )
  );
}
