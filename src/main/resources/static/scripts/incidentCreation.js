document.getElementById("incidentForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent the default form submission

    // Get the incident details from the form
    var title = document.getElementById("title").value;
    var description = document.getElementById("description").value;
    // Get other incident details...

    // Create an object with the incident data
    var newIncident = {
        title: title,
        description: description
        // Add other incident properties...
    };

    // Send the incident data to your backend API
    fetch('/api/incidents', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(newIncident),
    })
    .then(response => response.json())
    .then(data => {
        // Handle successful creation (e.g., show success message, update UI, etc.)
        console.log('Incident created:', data);
        // Close the modal or form after successful creation
        document.getElementById("addIncidentModal").style.display = "none";
    })
    .catch(error => {
        // Handle error (e.g., show error message, log error, etc.)
        console.error('Error creating incident:', error);
    });
});