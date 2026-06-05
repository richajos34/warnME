import { useEffect, useRef } from 'react';

const BERKELEY_CENTER = [37.8715, -122.2608];

function hasLocation(incident) {
  return incident.location && Number.isFinite(incident.location.x) && Number.isFinite(incident.location.y);
}

function createPopupContent(incident) {
  return `<strong>${incident.title || incident.displayType || 'Incident'}</strong><br>${incident.approximateLocation || incident.status || 'Unknown status'}`;
}

function getSeverityClassName(incident) {
  const alertStatus = incident.alertStatus || 'active';
  return `severity-marker severity-marker-${alertStatus}`;
}

function createSeverityIcon(incident) {
  return window.L.divIcon({
    className: getSeverityClassName(incident),
    html: '<span></span>',
    iconSize: [18, 18],
    iconAnchor: [9, 9],
    popupAnchor: [0, -10],
  });
}

export function useLeafletIncidentMap({ incidents, selectedIncident, onSelectIncident }) {
  const mapNodeRef = useRef(null);
  const mapRef = useRef(null);
  const markersRef = useRef(new Map());

  useEffect(() => {
    if (!mapNodeRef.current || mapRef.current || !window.L) {
      return undefined;
    }

    const map = window.L.map(mapNodeRef.current).setView(BERKELEY_CENTER, 15);
    window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);
    mapRef.current = map;

    return () => {
      map.remove();
      mapRef.current = null;
      markersRef.current.clear();
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !window.L) {
      return;
    }

    markersRef.current.forEach((marker) => marker.remove());
    markersRef.current.clear();

    const bounds = [];
    incidents.forEach((incident) => {
      if (!hasLocation(incident)) {
        return;
      }

      const incidentKey = incident.id || `${incident.title}-${incident.timestamp}`;
      const latLng = [incident.location.x, incident.location.y];
      bounds.push(latLng);

      const marker = window.L.marker(latLng, {
        icon: createSeverityIcon(incident),
      }).addTo(map).bindPopup(createPopupContent(incident));
      marker.on('click', () => onSelectIncident(incident));
      markersRef.current.set(incidentKey, marker);
    });

    if (bounds.length > 0) {
      map.fitBounds(bounds, { padding: [30, 30], maxZoom: 16 });
    }
  }, [incidents, onSelectIncident]);

  useEffect(() => {
    if (!selectedIncident) {
      return;
    }

    const incidentKey = selectedIncident.id || `${selectedIncident.title}-${selectedIncident.timestamp}`;
    const marker = markersRef.current.get(incidentKey);
    if (marker) {
      marker.openPopup();
    }
  }, [selectedIncident]);

  return mapNodeRef;
}
