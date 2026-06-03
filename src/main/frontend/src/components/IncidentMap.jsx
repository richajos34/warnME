import React from 'react';
import { useLeafletIncidentMap } from '../hooks/useLeafletIncidentMap.js';

export function IncidentMap({ incidents, selectedIncident, onSelectIncident }) {
  const mapNodeRef = useLeafletIncidentMap({
    incidents,
    selectedIncident,
    onSelectIncident,
  });

  return React.createElement('div', {
    id: 'map',
    ref: mapNodeRef,
    'aria-label': 'Campus incident map',
  });
}
