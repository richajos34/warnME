export function getIncidentKey(incident) {
  return incident.id || `${incident.title}-${incident.timestamp}`;
}

export function getIncidentCardTitle(incident) {
  return incident.title || incident.displayType || incident.type || 'Incident';
}

export function getIncidentTypeLabel(incident) {
  return incident.displayType || incident.type || incident.title || 'Incident';
}

export function getIncidentLocationLabel(incident) {
  return incident.approximateLocation || incident.locationText || incident.locationName || incident.address || incident.place || 'Campus area';
}

export function getIncidentSeverity(incident) {
  if (incident.severity) {
    const labels = {
      low: 'Low',
      medium: 'Medium',
      high: 'High',
      critical: 'Critical',
    };
    return labels[incident.severity] || incident.severity;
  }

  const searchableText = [incident.type, incident.title, incident.description, incident.status]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();

  if (searchableText.includes('assault') || searchableText.includes('weapon') || searchableText.includes('robbery')) {
    return 'High Risk';
  }

  if (searchableText.includes('theft') || searchableText.includes('break-in') || searchableText.includes('suspicious')) {
    return 'Moderate Risk';
  }

  return 'Monitor';
}

export function isCriticalAlert(incident) {
  return incident.severity === 'critical'
    || incident.severity === 'high'
    || /critical alert|critical alerts|avoid the area/i.test(`${incident.title || ''} ${incident.description || ''}`);
}

export function parseIncidentDate(incident) {
  if (incident.incidentDate) {
    const dateText = incident.incidentTime
      ? `${incident.incidentDate}T${incident.incidentTime}:00`
      : incident.incidentDate;
    const parsedStructuredDate = new Date(dateText);
    if (!Number.isNaN(parsedStructuredDate.getTime())) {
      return parsedStructuredDate;
    }
  }

  if (!incident.timestamp) {
    return null;
  }

  const parsedDate = new Date(incident.timestamp);
  if (!Number.isNaN(parsedDate.getTime())) {
    return parsedDate;
  }

  const timeOnlyMatch = incident.timestamp
    .trim()
    .match(/^(\d{1,2}):(\d{2})(?:\s?(AM|PM))?$/i);

  if (!timeOnlyMatch) {
    return null;
  }

  const [, hourValue, minuteValue, meridiem] = timeOnlyMatch;
  let hours = Number(hourValue);
  const minutes = Number(minuteValue);

  if (meridiem?.toLowerCase() === 'pm' && hours < 12) {
    hours += 12;
  }

  if (meridiem?.toLowerCase() === 'am' && hours === 12) {
    hours = 0;
  }

  if (hours > 23 || minutes > 59) {
    return null;
  }

  const today = new Date();
  today.setHours(hours, minutes, 0, 0);
  return today;
}

export function formatIncidentTime(incident) {
  const parsedDate = parseIncidentDate(incident);

  if (!parsedDate) {
    return incident.timestamp || 'Recently reported';
  }

  return parsedDate.toLocaleTimeString([], {
    hour: 'numeric',
    minute: '2-digit',
  });
}

export function formatTimeAgo(incident) {
  const parsedDate = parseIncidentDate(incident);

  if (!parsedDate) {
    return incident.timestamp || 'Recently';
  }

  const elapsedMinutes = Math.max(0, Math.round((Date.now() - parsedDate.getTime()) / 60000));

  if (elapsedMinutes < 1) {
    return 'Just now';
  }

  if (elapsedMinutes < 60) {
    return `${elapsedMinutes} minutes ago`;
  }

  const elapsedHours = Math.round(elapsedMinutes / 60);
  if (elapsedHours < 24) {
    return `${elapsedHours} hours ago`;
  }

  const elapsedDays = Math.round(elapsedHours / 24);
  return `${elapsedDays} days ago`;
}

export function isIncidentFromToday(incident) {
  const parsedDate = parseIncidentDate(incident);
  if (!parsedDate) {
    return false;
  }

  const today = new Date();
  return (
    parsedDate.getFullYear() === today.getFullYear() &&
    parsedDate.getMonth() === today.getMonth() &&
    parsedDate.getDate() === today.getDate()
  );
}

export function isIncidentActive(incident) {
  const status = (incident.status || '').toLowerCase();
  return !status.includes('closed') && !status.includes('resolved');
}

export function isIncidentVerified(incident) {
  if (incident.verificationStatus === 'official_warnme' || incident.verificationStatus === 'moderator_verified') {
    return true;
  }

  const status = (incident.status || '').toLowerCase();
  return status.includes('verified') || status.includes('confirmed') || status.includes('ucpd');
}

export function getIncidentSourceLabel(incident) {
  if (incident.source === 'gmail_warnme') {
    return 'UC Berkeley WarnMe';
  }

  if (incident.source === 'manual_report') {
    return 'Manual Report';
  }

  return incident.source || 'SafeZone';
}

export function getVerificationStatusLabel(incident) {
  const labels = {
    unverified: 'Unverified',
    community_reported: 'Community Reported',
    official_warnme: 'Official WarnMe',
    moderator_verified: 'Moderator Verified',
  };

  return labels[incident.verificationStatus] || incident.status || 'Unknown';
}

export function getMostCommonIncidentType(incidents) {
  if (incidents.length === 0) {
    return 'No reports yet';
  }

  const counts = incidents.reduce((accumulator, incident) => {
    const type = getIncidentTypeLabel(incident);
    accumulator.set(type, (accumulator.get(type) || 0) + 1);
    return accumulator;
  }, new Map());

  return Array.from(counts.entries()).sort((left, right) => right[1] - left[1])[0][0];
}
