# SafeZone UI Modernization

## Project Overview

SafeZone is a UC Berkeley campus safety application that helps students, staff, and visitors understand nearby safety conditions. The current target stack includes Spring Boot, Java, React, JavaScript, CSS, MongoDB, Leaflet.js, Docker, and AWS. The frontend is being migrated from static HTML/CSS and Bootstrap-driven pages to React-based screens served by Spring Boot.

The product centers on an interactive incident map, filtering and searching incidents, user-submitted and UCPD-confirmed reports, routing around dangerous areas, chatbot integration, and SMS alerts.

## Current Pain Points

- The map experience is functional but visually dated and difficult to scan.
- Incident status, source, severity, and recency are not clearly distinguished.
- Search and filtering are limited and not optimized for common safety workflows.
- User-submitted reports and UCPD-confirmed reports need clearer trust labeling.
- Mobile usability needs stronger prioritization for on-campus use.
- Routing, chatbot, and alert experiences need clearer entry points.
- Accessibility requirements are not yet consistently documented or enforced.

## Modernization Goals

- Create a mobile-first map interface that makes nearby risk easy to understand.
- Improve trust and safety clarity through visible source, status, and confirmation labels.
- Support fast incident filtering by type, status, time, source, and proximity.
- Make report submission clear, guided, and low-friction.
- Introduce consistent visual patterns for cards, markers, filters, forms, and alerts.
- Prepare the UI for chatbot, routing, and SMS alert workflows.
- Preserve current backend behavior while improving frontend structure incrementally.
- Replace static HTML page scripts with React components in small, testable steps.

## User Personas

- Student commuter: needs quick awareness of incidents near class routes and transit stops.
- On-campus resident: wants timely alerts and safer routes around dorms and late-night areas.
- Visitor or parent: needs simple, trustworthy safety context without knowing campus geography.
- UCPD or safety staff: needs clear distinction between confirmed incidents and community reports.
- Accessibility-focused user: needs keyboard navigation, readable contrast, and screen-reader-friendly status information.

## Key User Flows

- View nearby incidents on a campus map.
- Search for an incident, building, or area.
- Filter incidents by type, severity, status, source, and date range.
- Open an incident detail view with source, timestamp, status, location, and safety guidance.
- Submit a new incident report with location and category.
- Request a route that avoids dangerous or active incident areas.
- Ask the chatbot about nearby safety conditions.
- Subscribe to SMS alerts for selected locations or incident categories.

## Planned Screens

- Map dashboard
- Incident detail panel
- Filter and search panel
- Report incident flow
- Safe routing view
- Chatbot assistant panel
- SMS alert preferences
- Admin or review queue for submitted reports

## Mobile-First Requirements

- Primary map controls must be reachable with one hand.
- Filters should use bottom sheets or compact panels on small screens.
- Incident details should open in a dismissible bottom drawer.
- Marker taps must have large touch targets and clear selected states.
- Search should remain accessible without covering essential map content.
- Loading, empty, and error states must be visible and actionable.

## Accessibility Requirements

- Meet WCAG AA color contrast for text, controls, markers, and status labels.
- Support keyboard navigation for search, filters, incident lists, and dialogs.
- Provide screen-reader labels for map controls, filters, alerts, and incident status.
- Do not rely on color alone to communicate severity or source.
- Use clear focus states for all interactive elements.
- Provide reduced-motion-friendly interactions for drawers, alerts, and route changes.

## Phased Implementation Milestones

### Phase 1: Stabilize Current Map Experience

- Verify incidents load reliably from the API.
- Move the incident map page into a React-mounted frontend shell.
- Center and fit the map to campus incident data.
- Add loading, empty, and error states.
- Improve marker visibility and selected-marker behavior.

### Phase 2: Modernize Incident Discovery

- Add filter controls for type, status, source, and time range.
- Add a responsive incident list or drawer.
- Improve search behavior and no-results feedback.
- Define visual styles for incident severity and confirmation state.

### Phase 3: Improve Reporting and Trust Signals

- Redesign report submission as a guided flow.
- Distinguish user-submitted, pending, and UCPD-confirmed incidents.
- Add copy that explains report review and safety limitations.
- Add validation and success/error messaging.

### Phase 4: Add Routing, Chatbot, and Alerts

- Add routing UI for avoiding active incident areas.
- Add chatbot entry point and conversation panel.
- Add SMS alert preference UI.
- Connect workflows to backend services incrementally.

### Phase 5: Polish, Test, and Prepare for Deployment

- Add frontend regression coverage where practical.
- Perform accessibility audit.
- Verify responsive behavior across mobile and desktop.
- Prepare Docker and AWS deployment notes for the modernized UI.

## Initial Ticket Backlog

- Audit current map, incident API, static assets, and frontend dependencies.
- Add loading/error/empty UI states for incident fetches.
- Fit map bounds to loaded incidents and improve marker styling.
- Create mobile incident detail drawer.
- Add incident type and status filters.
- Add source labels for user-submitted and UCPD-confirmed reports.
- Redesign report incident form with validation and confirmation state.
- Add route-avoidance UI prototype.
- Add chatbot panel placeholder and integration contract.
- Add SMS alert preferences screen.
- Add accessibility checklist to PR review workflow.
- Add frontend smoke test for map page and incident fetch.
