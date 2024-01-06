        var map = L.map('map').setView([0, 0], 2); // Default coordinates and zoom level
        console.log("Map created");
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);

        function onLocationFound(e) {
            var radius = e.accuracy / 2;
            L.marker(e.latlng).addTo(map)
                .bindPopup("You are within " + radius + " meters from this point").openPopup();
            map.setView(e.latlng, 15); // Set map view to the found location with a specific zoom level
        }

        function onLocationError(e) {
            alert(e.message);
        }

        // Check for geolocation support
        if ('geolocation' in navigator) {
            // Request current location
            navigator.geolocation.getCurrentPosition(onLocationFound, onLocationError);
        } else {
            alert("Geolocation is not supported by this browser.");
        }
        // Call function to fetch incident data
        fetchIncidentsData();

        function searchLocation() {
            var searchTerm = document.getElementById('searchInput').value;
            if (searchTerm.trim() !== '') {
                // Fetch API data based on the search term (replace with your API endpoint)
                fetch(`http://localhost:8080/api/incidents/?title=${searchTerm}`)
                    .then(response => response.json())
                    .then(data => {
                        // Check if data contains location details
                        if (data && data.location) {
                            var lat = data.location.x;
                            var lon = data.location.y;
                            map.setView([lat, lon], 20); // Set map view to the location with zoom level 10 (adjust as needed)
                        } else {
                            alert('Location not found for the searched title.');
                        }
                    })
                    .catch(error => console.error('Error searching location:', error));
            } else {
                alert('Please enter a title to search.');
            }
        }