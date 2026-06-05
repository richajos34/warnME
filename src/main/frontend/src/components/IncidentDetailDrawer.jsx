import React from 'react';
import {
  getIncidentSourceLabel,
  getIncidentCardTitle,
  getIncidentTypeLabel,
  getVerificationStatusLabel,
} from '../utils/incidents.js';

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
    React.createElement('h2', null, getIncidentCardTitle(incident)),
    React.createElement('p', null, incident.description || 'No description available.'),
    React.createElement(
      'dl',
      null,
      React.createElement('dt', null, 'Type'),
      React.createElement('dd', null, getIncidentTypeLabel(incident)),
      React.createElement('dt', null, 'Severity'),
      React.createElement('dd', null, incident.severity || 'Unknown'),
      React.createElement('dt', null, 'Alert'),
      React.createElement('dd', null, incident.alertStatus || 'active'),
      React.createElement('dt', null, 'Location'),
      React.createElement('dd', null, incident.approximateLocation || incident.locationText || 'Campus area'),
      React.createElement('dt', null, 'Date'),
      React.createElement('dd', null, incident.incidentDate || 'Unknown'),
      React.createElement('dt', null, 'Time'),
      React.createElement('dd', null, incident.incidentTime || incident.timestamp || 'Unknown'),
      React.createElement('dt', null, 'Verified'),
      React.createElement('dd', null, getVerificationStatusLabel(incident)),
      React.createElement('dt', null, 'Source'),
      React.createElement('dd', null, getIncidentSourceLabel(incident))
    ),
    React.createElement(
      'button',
      { className: 'secondary-action', type: 'button', onClick: onShowPhotos },
      'Nearby photos'
    )
  );
}
