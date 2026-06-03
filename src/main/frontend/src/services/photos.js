export async function fetchNearbyPhotos(location) {
  const endpoint = new URL('/api/photos/nearby', window.location.origin);
  endpoint.searchParams.set('lat', location.x);
  endpoint.searchParams.set('lon', location.y);

  const response = await fetch(endpoint);
  if (!response.ok) {
    throw new Error(`Photo API returned ${response.status}`);
  }

  const data = await response.json();
  return data.photos || [];
}
