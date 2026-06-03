import React from 'react';

const BERKELEY_SEAL_URL =
  'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Seal_of_University_of_California%2C_Berkeley.svg/1200px-Seal_of_University_of_California%2C_Berkeley.svg.png';

const NAV_ITEMS = [
  { label: 'Home', href: '#home' },
  { label: 'Map', href: '#live-map' },
  { label: 'Report Incident', href: '#report-incident' },
  { label: 'Alerts', href: '#sms-alerts' },
  { label: 'Gmail Import', href: '#gmail-ingestion' },
  { label: 'About', href: '#about' },
];

export function AppHeader() {
  return (
    <header className="app-header">
      <a className="brand" href="#home" aria-label="SafeZone home">
        <img src={BERKELEY_SEAL_URL} alt="UC Berkeley seal" />
        <div>
          <h1>SafeZone</h1>
          <p>UC Berkeley incident awareness</p>
        </div>
      </a>
      <nav className="top-nav" aria-label="Primary navigation">
        {NAV_ITEMS.map((item) => (
          <a key={item.href} href={item.href}>
            {item.label}
          </a>
        ))}
      </nav>
    </header>
  );
}
