const FLICKR_API_KEY = '8c662afa13804f7f08f850a74c26a357';

export async function fetchNearbyPhotos(location) {
  const endpoint = new URL('https://www.flickr.com/services/rest/');
  endpoint.searchParams.set('method', 'flickr.photos.search');
  endpoint.searchParams.set('api_key', FLICKR_API_KEY);
  endpoint.searchParams.set('lat', location.x);
  endpoint.searchParams.set('lon', location.y);
  endpoint.searchParams.set('format', 'json');
  endpoint.searchParams.set('nojsoncallback', '1');
  endpoint.searchParams.set('per_page', '10');
  endpoint.searchParams.set('sort', 'date-posted-desc');

  const response = await fetch(endpoint);
  const data = await response.json();

  if (!data.photos || data.photos.photo.length === 0) {
    return [];
  }

  return data.photos.photo.map((photo) => {
    return `https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg`;
  });
}
