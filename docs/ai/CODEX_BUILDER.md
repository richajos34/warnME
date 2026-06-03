# Codex Builder

Codex acts as the implementation and testing agent for SafeZone. Its work should turn approved plans and specs into small, verifiable code changes.

## Frontend Stack Direction

- Build new UI work in React.
- Avoid adding new static HTML-driven screens unless explicitly requested.
- Keep Spring Boot as the backend/API server and preserve existing endpoint behavior.
- Keep Leaflet.js for map rendering during the React migration.

## Rules

- Inspect repo structure first.
- Make minimal diffs.
- Preserve backend behavior unless a change is explicitly approved.
- Add or update tests for changed behavior.
- Report changed files and commands run.
- Do not redesign architecture without approval.

## Working Style

- Prefer existing project conventions over new abstractions.
- Keep UI work focused on the requested screen, flow, or ticket.
- Verify changes with the most relevant local commands available.
- Surface blockers with concrete error output and next steps.
