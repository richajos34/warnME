import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IncidentServiceHttpTest {

    @LocalServerPort
    private int port = 8080;

    @Autowired
    private RestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/"; // Update the base URL based on your service endpoint
    }

    @Test
    public void testGetAllIncidents() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getBaseUrl() + "incidents", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // Perform assertions on the response content or status code
        // For example: assertTrue(responseEntity.getBody().contains("Expected data"));
    }

    // Write more test cases for other service endpoints like getIncidentById, createIncident, deleteIncident, etc.
}
