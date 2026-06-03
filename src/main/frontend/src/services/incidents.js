export async function fetchIncidents() {
  const response = await fetch('/api/incidents');

  if (!response.ok) {
    throw new Error(`Incident API returned ${response.status}`);
  }

  return response.json();
}
