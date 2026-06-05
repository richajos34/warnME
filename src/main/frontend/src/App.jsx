import React, { useCallback, useMemo, useState } from 'react';
import { AppHeader } from './components/AppHeader.jsx';
import { IncidentDetailDrawer } from './components/IncidentDetailDrawer.jsx';
import { IncidentMap } from './components/IncidentMap.jsx';
import { IncidentPanel } from './components/IncidentPanel.jsx';
import { PhotoModal } from './components/PhotoModal.jsx';
import { useIncidents } from './hooks/useIncidents.js';
import { fetchNearbyPhotos } from './services/photos.js';
import { fetchGmailStatus, importWarnMeEmails, searchWarnMeEmails, syncWarnMeEmails } from './services/gmail.js';
import {
  formatIncidentTime,
  getIncidentKey,
  getIncidentLocationLabel,
  getIncidentSeverity,
  getIncidentTypeLabel,
  getMostCommonIncidentType,
  isCriticalAlert,
  isIncidentActive,
  isIncidentFromToday,
  isIncidentVerified,
  parseIncidentDate,
} from './utils/incidents.js';

const QUICK_ACTIONS = [
  {
    title: 'View Live Map',
    description: 'See active campus reports and nearby areas to avoid.',
    href: '#live-map',
  },
  {
    title: 'Report Incident',
    description: 'Share a safety concern so others can stay aware.',
    href: '#report-incident',
  },
  {
    title: 'Sign Up for SMS Alerts',
    description: 'Get urgent campus safety updates on your phone.',
    href: '#sms-alerts',
  },
  {
    title: 'Plan Safe Route',
    description: 'Use incident context before walking across campus.',
    href: '#safe-route',
  },
];

function getInitialGmailStatus() {
  const searchParams = new URLSearchParams(window.location.search);

  if (searchParams.has('gmail_error')) {
    return {
      connected: false,
      message: `Gmail connection failed: ${searchParams.get('gmail_error') || 'Unknown error'}`,
    };
  }

  if (searchParams.get('gmail') === 'connected') {
    return {
      connected: true,
      message: 'Berkeley Gmail connected. You can now search for WarnMe emails.',
    };
  }

  return {
    connected: false,
    message: '',
  };
}

function sortIncidentsByRecency(incidents) {
  return [...incidents].sort((left, right) => {
    const leftDate = parseIncidentDate(left);
    const rightDate = parseIncidentDate(right);

    if (!leftDate && !rightDate) {
      return 0;
    }

    if (!leftDate) {
      return 1;
    }

    if (!rightDate) {
      return -1;
    }

    return rightDate.getTime() - leftDate.getTime();
  });
}

function getSafetyScore(activeIncidentCount) {
  if (activeIncidentCount === 0) {
    return 'Clear';
  }

  if (activeIncidentCount <= 3) {
    return 'Watch';
  }

  return 'Elevated';
}

function buildDashboardStats(incidents) {
  const activeIncidents = incidents.filter(isIncidentActive);
  const newToday = incidents.filter(isIncidentFromToday);
  const verifiedReports = incidents.filter(isIncidentVerified);

  return {
    safetyScore: getSafetyScore(activeIncidents.length),
    activeIncidentCount: activeIncidents.length,
    newTodayCount: newToday.length,
    verifiedReportCount: verifiedReports.length,
    mostCommonIncident: getMostCommonIncidentType(incidents),
    safestArea: activeIncidents.length === 0 ? 'Campus-wide' : 'Areas without active reports',
    highestActivityArea: activeIncidents.length > 0 ? getIncidentLocationLabel(activeIncidents[0]) : 'No active hotspot',
  };
}

function HeroSafetySummary({ stats }) {
  const supportingItems = [
    { label: 'Active Incidents', value: stats.activeIncidentCount },
    { label: 'New Incidents Today', value: stats.newTodayCount },
    { label: 'Verified Reports', value: stats.verifiedReportCount },
  ];

  return (
    <section className="hero-summary" id="home" aria-labelledby="hero-title">
      <div className="hero-copy">
        <p className="eyebrow">UC Berkeley safety dashboard</p>
        <h2 id="hero-title">Know what is happening nearby before you cross campus.</h2>
        <p>
          SafeZone turns live incident reports into a quick safety readout, a campus map, and clear actions for
          reporting concerns or getting alerts.
        </p>
        <div className="hero-actions" aria-label="Primary safety actions">
          <a className="primary-action" href="#live-map">
            View Live Map
          </a>
          <a className="secondary-action" href="#report-incident">
            Report Incident
          </a>
        </div>
      </div>
      <div className="safety-command-center" aria-label="Campus safety summary">
        <article className="safety-score-panel">
          <span>Current Safety Score</span>
          <strong>{stats.safetyScore}</strong>
          <p>
            {stats.activeIncidentCount === 0
              ? 'No active incidents are currently shown.'
              : `${stats.activeIncidentCount} active incident${stats.activeIncidentCount === 1 ? '' : 's'} need awareness.`}
          </p>
        </article>
        <div className="summary-stack">
          {supportingItems.map((item) => (
            <article className="summary-row" key={item.label}>
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </article>
          ))}
        </div>
        <article className="next-step-panel">
          <span>Best next step</span>
          <strong>Check the map before moving.</strong>
          <a href="#live-map">Open live map</a>
        </article>
      </div>
    </section>
  );
}

function QuickActions() {
  const [primaryAction, ...secondaryActions] = QUICK_ACTIONS;

  return (
    <section className="dashboard-section" aria-labelledby="quick-actions-title">
      <div className="section-heading">
        <p className="eyebrow">Choose an action</p>
        <h2 id="quick-actions-title">What do you need right now?</h2>
      </div>
      <div className="quick-action-layout">
        <a className="action-card action-card-primary" href={primaryAction.href}>
          <strong>{primaryAction.title}</strong>
          <span>{primaryAction.description}</span>
        </a>
        <div className="secondary-action-list">
          {secondaryActions.map((action) => (
          <a
            className="action-card"
            href={action.href}
            key={action.title}
            id={action.title === 'Report Incident' ? 'report-incident' : undefined}
          >
            <strong>{action.title}</strong>
            <span>{action.description}</span>
          </a>
          ))}
        </div>
      </div>
    </section>
  );
}

function SafetyInsights({ stats }) {
  const insights = [
    { label: 'Most Common Incident', value: stats.mostCommonIncident },
    { label: 'Safest Area', value: stats.safestArea },
    { label: 'Highest Activity Area', value: stats.highestActivityArea },
    { label: 'New Reports Today', value: stats.newTodayCount },
  ];

  return (
    <section className="dashboard-section" aria-labelledby="insights-title">
      <div className="section-heading">
        <p className="eyebrow">Safety insights</p>
        <h2 id="insights-title">What to know right now</h2>
      </div>
      <div className="insight-grid">
        {insights.map((insight) => (
          <article className="insight-card" key={insight.label}>
            <span>{insight.label}</span>
            <strong>{insight.value}</strong>
          </article>
        ))}
      </div>
    </section>
  );
}

function RecentActivityTimeline({ incidents }) {
  const timelineIncidents = incidents.slice(0, 6);

  return (
    <section className="dashboard-section" aria-labelledby="activity-title">
      <div className="section-heading">
        <p className="eyebrow">Recent activity</p>
        <h2 id="activity-title">Latest campus reports</h2>
      </div>
      {timelineIncidents.length === 0 ? (
        <p className="empty-state">No recent activity is available yet.</p>
      ) : (
        <ol className="activity-timeline">
          {timelineIncidents.map((incident) => (
            <li key={getIncidentKey(incident)}>
              <time>{formatIncidentTime(incident)}</time>
              <span>{getIncidentTypeLabel(incident)}</span>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}

function DashboardSupport({ stats, incidents }) {
  return (
    <section className="support-grid" aria-label="Safety summaries and recent activity">
      <SafetyInsights stats={stats} />
      <RecentActivityTimeline incidents={incidents} />
    </section>
  );
}

function SmsAlertSignup({ phoneNumber, onPhoneNumberChange, onSubmit }) {
  return (
    <section className="sms-alert-section" id="sms-alerts" aria-labelledby="sms-title">
      <div className="section-heading">
        <p className="eyebrow">Stay informed</p>
        <h2 id="sms-title">Get SMS alerts for urgent campus safety updates.</h2>
      </div>
      <form className="sms-form" onSubmit={onSubmit}>
        <label htmlFor="sms-phone">Phone number</label>
        <div className="sms-controls">
          <input
            id="sms-phone"
            type="tel"
            placeholder="(510) 555-0123"
            value={phoneNumber}
            onChange={(event) => onPhoneNumberChange(event.target.value)}
          />
          <button type="submit">Subscribe</button>
        </div>
      </form>
    </section>
  );
}

function GmailIngestionPanel({
  gmailStatus,
  searchStatus,
  searchError,
  warnMeSearchResult,
  importStatus,
  importError,
  warnMeImportResult,
  onSearchWarnMeEmails,
  onImportWarnMeEmails,
}) {
  const messages = warnMeSearchResult?.messages || [];
  const importSummary = warnMeImportResult?.summary;

  return (
    <section className="gmail-ingestion-section" id="gmail-ingestion" aria-labelledby="gmail-ingestion-title">
      <div className="section-heading">
        <p className="eyebrow">WarnMe import</p>
        <h2 id="gmail-ingestion-title">Connect Berkeley Gmail</h2>
      </div>
      <p className="section-copy">Import UC Berkeley WarnMe alerts from your Gmail history.</p>
      <div className="gmail-consent-summary" aria-label="Gmail import privacy summary">
        <div>
          <strong>SafeZone will only search:</strong>
          <span>from: ucberkeley@warnme.berkeley.edu</span>
          <span>subject begins with: UC Berkeley WarnMe</span>
          <span>time range: past year</span>
        </div>
        <div>
          <strong>SafeZone will not:</strong>
          <span>ask for your Gmail password</span>
          <span>send, delete, or modify emails</span>
          <span>intentionally read unrelated email content</span>
        </div>
      </div>
      {gmailStatus.message && <p className="status-message">{gmailStatus.message}</p>}
      <div className="gmail-actions">
        <a className="primary-action" href="/api/gmail/connect">
          Connect Gmail
        </a>
        <button
          className="secondary-action"
          type="button"
          onClick={onSearchWarnMeEmails}
          disabled={searchStatus === 'loading' || importStatus === 'loading'}
        >
          {searchStatus === 'loading' ? 'Searching...' : 'Search WarnMe Emails'}
        </button>
        <button
          className="secondary-action"
          type="button"
          onClick={onImportWarnMeEmails}
          disabled={searchStatus === 'loading' || importStatus === 'loading'}
        >
          {importStatus === 'loading' ? 'Importing...' : 'Import WarnMe Emails'}
        </button>
      </div>
      {searchError && <p className="error-text">{searchError}</p>}
      {importError && <p className="error-text">{importError}</p>}
      {importSummary && (
        <div className="gmail-results" aria-live="polite">
          <p>
            Imported {importSummary.savedCount} new WarnMe incident
            {importSummary.savedCount === 1 ? '' : 's'}.
          </p>
          <div className="import-summary-grid">
            <span>Emails found: {importSummary.emailsFound}</span>
            <span>Parsed: {importSummary.parsedCount}</span>
            <span>Geocoded: {importSummary.geocodedCount}</span>
            <span>Duplicates: {importSummary.duplicateCount}</span>
            <span>Failed: {importSummary.failedCount}</span>
          </div>
          {importSummary.failures?.length > 0 && (
            <ol className="gmail-message-list">
              {importSummary.failures.map((failure) => (
                <li key={`${failure.gmailMessageId}-${failure.reason}`}>
                  <strong>{failure.gmailMessageId || 'Unknown message'}</strong>
                  <span>{failure.reason}</span>
                </li>
              ))}
            </ol>
          )}
        </div>
      )}
      {warnMeSearchResult && (
        <div className="gmail-results" aria-live="polite">
          <p>
            {warnMeSearchResult.count === 0
              ? 'No matching WarnMe emails found.'
              : `${warnMeSearchResult.count} matching WarnMe email${warnMeSearchResult.count === 1 ? '' : 's'} found.`}
          </p>
          {messages.length > 0 && (
            <ol className="gmail-message-list">
              {messages.map((message) => (
                <li key={message.messageId}>
                  <strong>{message.subject}</strong>
                  <span>{message.sender}</span>
                  <span>{message.receivedDate || message.internalTimestamp}</span>
                  <p>{message.snippet}</p>
                </li>
              ))}
            </ol>
          )}
        </div>
      )}
    </section>
  );
}

export function App() {
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [photoUrl, setPhotoUrl] = useState('');
  const [isPhotoOpen, setIsPhotoOpen] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [gmailStatus] = useState(getInitialGmailStatus);
  const [isGmailConnected, setIsGmailConnected] = useState(false);
  const [gmailSearchStatus, setGmailSearchStatus] = useState('idle');
  const [gmailSearchError, setGmailSearchError] = useState('');
  const [warnMeSearchResult, setWarnMeSearchResult] = useState(null);
  const [gmailImportStatus, setGmailImportStatus] = useState('idle');
  const [gmailImportError, setGmailImportError] = useState('');
  const [warnMeImportResult, setWarnMeImportResult] = useState(null);
  const [showAllAlerts, setShowAllAlerts] = useState(false);
  const { incidents, status, error, reloadIncidents } = useIncidents();

  const sortedIncidents = useMemo(() => sortIncidentsByRecency(incidents), [incidents]);
  const visibleIncidents = useMemo(() => (
    showAllAlerts ? sortedIncidents : sortedIncidents.filter(isCriticalAlert)
  ), [showAllAlerts, sortedIncidents]);
  const dashboardStats = useMemo(() => buildDashboardStats(incidents), [incidents]);

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

  const handleSmsSubmit = useCallback(
    (event) => {
      event.preventDefault();
      window.alert(
        phoneNumber.trim()
          ? 'SMS alert signup is not connected yet.'
          : 'Enter a phone number to subscribe to SMS alerts.'
      );
    },
    [phoneNumber]
  );

  const handleSearchWarnMeEmails = useCallback(async () => {
    setGmailSearchStatus('loading');
    setGmailSearchError('');

    try {
      const result = await searchWarnMeEmails();
      setWarnMeSearchResult(result);
      setGmailSearchStatus('ready');
    } catch (searchError) {
      setWarnMeSearchResult(null);
      setGmailSearchStatus('error');
      setGmailSearchError(searchError.message);
    }
  }, []);

  const handleImportWarnMeEmails = useCallback(async () => {
    setGmailImportStatus('loading');
    setGmailImportError('');

    try {
      const result = await importWarnMeEmails();
      setWarnMeImportResult(result);
      setIsGmailConnected(result.connected !== false);
      setGmailImportStatus('ready');
      await reloadIncidents();
    } catch (importError) {
      setWarnMeImportResult(null);
      setGmailImportStatus('error');
      setGmailImportError(importError.message);
    }
  }, [reloadIncidents]);

  React.useEffect(() => {
    fetchGmailStatus()
      .then((statusResult) => {
        setIsGmailConnected(Boolean(statusResult.connected));
      })
      .catch(() => {
        setIsGmailConnected(false);
      });
  }, []);

  React.useEffect(() => {
    if (!isGmailConnected) {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      syncWarnMeEmails()
        .then((result) => {
          if (result.connected === false) {
            setIsGmailConnected(false);
            return;
          }
          return reloadIncidents();
        })
        .catch(() => undefined);
    }, 60000);

    return () => window.clearInterval(intervalId);
  }, [isGmailConnected, reloadIncidents]);

  return (
    <>
      <AppHeader />
      <main className="dashboard-shell">
        <HeroSafetySummary stats={dashboardStats} />
        <QuickActions />
        <section className="main-dashboard-grid" id="live-map" aria-labelledby="live-map-title">
          <div className="map-area">
            <div className="section-heading">
              <p className="eyebrow">Live map</p>
              <h2 id="live-map-title">Explore active campus reports</h2>
              <p className="section-copy">
                {showAllAlerts
                  ? 'Showing all campus alerts.'
                  : 'Showing critical WarnMe alerts first. Toggle all alerts for the full feed.'}
              </p>
              <label className="alert-toggle">
                <input
                  type="checkbox"
                  checked={showAllAlerts}
                  onChange={(event) => setShowAllAlerts(event.target.checked)}
                />
                Show all alerts
              </label>
            </div>
            <IncidentMap
              incidents={visibleIncidents}
              selectedIncident={selectedIncident}
              onSelectIncident={handleSelectIncident}
            />
          </div>
          <IncidentPanel
            incidents={visibleIncidents}
            status={status}
            error={error}
            selectedIncident={selectedIncident}
            onSelectIncident={handleSelectIncident}
          />
        </section>
        <DashboardSupport stats={dashboardStats} incidents={sortedIncidents} />
        <GmailIngestionPanel
          gmailStatus={gmailStatus}
          searchStatus={gmailSearchStatus}
          searchError={gmailSearchError}
          warnMeSearchResult={warnMeSearchResult}
          importStatus={gmailImportStatus}
          importError={gmailImportError}
          warnMeImportResult={warnMeImportResult}
          onSearchWarnMeEmails={handleSearchWarnMeEmails}
          onImportWarnMeEmails={handleImportWarnMeEmails}
        />
        <section className="dashboard-section" id="safe-route" aria-labelledby="route-title">
          <div className="section-heading">
            <p className="eyebrow">Plan safe route</p>
            <h2 id="route-title">Check nearby activity before choosing a route.</h2>
          </div>
          <p className="section-copy">
            Route planning will use active reports and high-activity areas to help students avoid avoidable risk.
          </p>
        </section>
        <SmsAlertSignup
          phoneNumber={phoneNumber}
          onPhoneNumberChange={setPhoneNumber}
          onSubmit={handleSmsSubmit}
        />
        <section className="dashboard-section" id="about" aria-labelledby="about-title">
          <div className="section-heading">
            <p className="eyebrow">About SafeZone</p>
            <h2 id="about-title">A campus safety dashboard for awareness and action.</h2>
          </div>
          <p className="section-copy">
            SafeZone combines user-submitted and confirmed campus reports so students can understand recent activity,
            report concerns, and subscribe to alerts from one place.
          </p>
        </section>
      </main>
      {selectedIncident && (
        <IncidentDetailDrawer
          incident={selectedIncident}
          onClose={handleCloseDetails}
          onShowPhotos={handleShowPhotos}
        />
      )}
      {isPhotoOpen && <PhotoModal photoUrl={photoUrl} onClose={handleClosePhoto} />}
    </>
  );
}
