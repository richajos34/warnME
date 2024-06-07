package com.berkeley.irms.warnme.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.services.IncidentService;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @Autowired
    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Incident getIncidentById(@PathVariable String id) {
        return incidentService.getIncidentById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));
    }

     @GetMapping("/")
    public Incident getIncidentByTitle(@RequestParam String title) {
        return incidentService.getIncidentByTitle(title)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + title));
    }

    @PostMapping
    public Incident createIncident(@RequestBody Incident incident) {
        return incidentService.createIncident(incident);
    }

    @DeleteMapping("/{id}")
    public void deleteIncident(@PathVariable String id) {
        incidentService.deleteIncident(id);
    }

}
