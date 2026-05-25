// Function to fetch incident data from backend API
function fetchIncidentsData() {
  fetch('/api/incidents')
    .then(response => {
      if (!response.ok) {
        throw new Error(`Incident API returned ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      var incidentBounds = [];

      // Loop through incident data and create markers
      data.forEach(function (incident) {
        if (!incident.location) {
          return;
        }

        var latLng = [incident.location.x, incident.location.y];
        incidentBounds.push(latLng);

        var marker = L.marker(latLng).addTo(map);
        marker.bindPopup(`
                <p>${incident.title}</p>
                <p>${incident.description}</p>
                <p>${incident.timestamp}</p>
                <p>${incident.status}</p>
                <p>${incident.type}</p>
                <button onclick="handlePopupButtonClick(${incident.location.x}, ${incident.location.y})">Photos</button>`);
      })

      if (incidentBounds.length > 0) {
        map.fitBounds(incidentBounds, { padding: [30, 30], maxZoom: 16 });
      }
    }
    ).catch(error => console.error('Error fetching incidents:', error));
}
