// Function to fetch incident data from backend API
function fetchIncidentsData() {
  fetch('http://localhost:8080/api/incidents') // Replace with your actual backend API endpoint
    .then(response => response.json())
    .then(data => {
      // Loop through incident data and create markers
      data.forEach(function (incident) {
        var marker = L.marker([incident.location.x, incident.location.y]).addTo(map);
        marker.bindPopup(`
                <p>${incident.title}</p>
                <p>${incident.description}</p>
                <p>${incident.timestamp}</p>
                <p>${incident.status}</p>
                <p>${incident.type}</p>
                <button onclick="handlePopupButtonClick(${incident.location.x}, ${incident.location.y})">Photos</button>`).openPopup();
      })

    }
    ).catch(error => console.error('Error fetching incidents:', error));
}

