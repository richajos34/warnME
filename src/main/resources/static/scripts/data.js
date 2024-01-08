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
                <button onclick="handlePopupButtonClick(${incident.location.x}, ${incident.location.y})">Photos</button>`).openPopup();
            })

        }
        ).catch(error => console.error('Error fetching incidents:', error));
}

// Function to handle button click within marker popup
function handlePopupButtonClick(lat, lon) {
    fetchFlickrPhotos(lat, lon).then(photoUrls => {
        if (photoUrls.length > 0) {
            var currentIndex = 0;

            window.nextPhoto = function() {
                currentIndex = (currentIndex + 1);
                updatePopupContent();
            }

            function updatePopupContent() {
                var marker = L.marker([lon+50, lat+50]).addTo(map);
                var popupContent = `<div><img src="${photoUrls[currentIndex]}" style="width: 200px; height: auto; margin: 5px;" />`;
                popupContent += `<button onclick="nextPhoto()">Next</button></div>`;

                // Update the popup content with Flickr photos
                marker.bindPopup(popupContent).openPopup();
            }

            updatePopupContent();
        } else {
            // If no photos found, display a message
            marker.bindPopup('<div>No photos found.</div>').openPopup();
        }
    });
}

var apiKey = "8c662afa13804f7f08f850a74c26a357";


// Function to fetch Flickr photos based on location
function fetchFlickrPhotos(lat, lon) {
  const flickrEndpoint = `https://www.flickr.com/services/rest/?method=flickr.photos.search&api_key=${apiKey}&lat=${lat}&lon=${lon}&format=json&nojsoncallback=1`;

  return fetch(flickrEndpoint)
    .then(response => response.json())
    .then(data => {
      if (data.photos && data.photos.photo.length > 0) {
        return data.photos.photo.map(photo =>
          `https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg`
        );
      } else {
        return [];
      }
    })
    .catch(error => {
      console.error('Error fetching photos:', error);
      return [];
    });
}

