import React from 'react';

export function IncidentDetailDrawer({ incident, onClose, onShowPhotos }) {
  return React.createElement(
    'section',
    { className: 'detail-drawer', 'aria-label': 'Selected incident details' },
    React.createElement(
      'button',
      {
        className: 'drawer-close',
        type: 'button',
        onClick: onClose,
        'aria-label': 'Close incident details',
      },
      'x'
    ),
    React.createElement('h2', null, incident.title || 'Incident'),
    React.createElement('p', null, incident.description || 'No description available.'),
    React.createElement(
      'dl',
      null,
      React.createElement('dt', null, 'Status'),
      React.createElement('dd', null, incident.status || 'Unknown'),
      React.createElement('dt', null, 'Type'),
      React.createElement('dd', null, incident.type || 'Uncategorized'),
      React.createElement('dt', null, 'Time'),
      React.createElement('dd', null, incident.timestamp || 'Unknown')
    ),
    React.createElement(
      'button',
      { className: 'secondary-action', type: 'button', onClick: onShowPhotos },
      'Nearby photos'
    )
  );
}
