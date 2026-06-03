import React, { useCallback, useMemo, useState } from 'react';
import { AppHeader } from './components/AppHeader.jsx';
import { IncidentDetailDrawer } from './components/IncidentDetailDrawer.jsx';
import { IncidentMap } from './components/IncidentMap.jsx';
import { IncidentPanel } from './components/IncidentPanel.jsx';
import { PhotoModal } from './components/PhotoModal.jsx';
import { useIncidents } from './hooks/useIncidents.js';
import { fetchNearbyPhotos } from './services/photos.js';

function matchesSearch(incident, searchTerm) {
  const normalizedTerm = searchTerm.trim().toLowerCase();
  if (!normalizedTerm) {
    return true;
  }

  return [incident.title, incident.description, incident.status, incident.type]
    .filter(Boolean)
    .some((value) => value.toLowerCase().includes(normalizedTerm));
}

export function App() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [photoUrl, setPhotoUrl] = useState('');
  const [isPhotoOpen, setIsPhotoOpen] = useState(false);
  const { incidents, status, error } = useIncidents();

  const visibleIncidents = useMemo(
    () => incidents.filter((incident) => matchesSearch(incident, searchTerm)),
    [incidents, searchTerm]
  );

  const handleSelectIncident = useCallback((incident) => {
    setSelectedIncident(incident);
  }, []);

  const handleCloseDetails = useCallback(() => {
    setSelectedIncident(null);
  }, []);

  const handleClosePhoto = useCallback(() => {
    setIsPhotoOpen(false);
    setPhotoUrl('');
  }, []);

  const handleShowPhotos = useCallback(async () => {
    if (!selectedIncident?.location) {
      return;
    }

    const photoUrls = await fetchNearbyPhotos(selectedIncident.location);
    if (photoUrls.length === 0) {
      window.alert('No photos found near this incident.');
      return;
    }

    setPhotoUrl(photoUrls[0]);
    setIsPhotoOpen(true);
  }, [selectedIncident]);

  return React.createElement(
    React.Fragment,
    null,
    React.createElement(AppHeader, {
      searchTerm,
      onSearchChange: setSearchTerm,
    }),
    React.createElement(
      'main',
      { className: 'map-shell' },
      React.createElement(IncidentMap, {
        incidents: visibleIncidents,
        selectedIncident,
        onSelectIncident: handleSelectIncident,
      })
    ),
    React.createElement(IncidentPanel, {
      incidents: visibleIncidents,
      status,
      error,
      selectedIncident,
      onSelectIncident: handleSelectIncident,
    }),
    selectedIncident &&
      React.createElement(IncidentDetailDrawer, {
        incident: selectedIncident,
        onClose: handleCloseDetails,
        onShowPhotos: handleShowPhotos,
      }),
    isPhotoOpen &&
      React.createElement(PhotoModal, {
        photoUrl,
        onClose: handleClosePhoto,
      })
  );
}
