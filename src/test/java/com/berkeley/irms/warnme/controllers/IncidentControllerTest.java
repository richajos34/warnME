package com.berkeley.irms.warnme.controllers;

import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.models.Location;
import com.berkeley.irms.warnme.services.CityService;
import com.berkeley.irms.warnme.services.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private CityService cityService;

    @Test
    void getAllIncidentsReturnsIncidentsFromService() throws Exception {
        Incident incident = new Incident(
                "Stolen goods",
                "Break in on Haste",
                new Location(37.8715f, -122.2730f),
                "09:00",
                "Ongoing",
                "Theft");
        when(incidentService.getIncidents(null, null, null, null, null)).thenReturn(List.of(incident));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Stolen goods"))
                .andExpect(jsonPath("$[0].location.x").value(37.8715));
    }

    @Test
    void getIncidentByIdReturns404WhenMissing() throws Exception {
        when(incidentService.getIncidentById("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/incidents/missing-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getIncidentByTitleReturns404WhenMissing() throws Exception {
        when(incidentService.getIncidentByTitle("missing-title")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/incidents/").param("title", "missing-title"))
                .andExpect(status().isNotFound());
    }
}
