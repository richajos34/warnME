package com.berkeley.irms.warnme.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.repositories.IncidentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    @Autowired
    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Optional<Incident> getIncidentById(String id) {
        return incidentRepository.findById(id);
    }

     public Optional<Incident> getIncidentByTitle(String title) {
        return incidentRepository.findFirstByTitleContainingIgnoreCase(title);
    }

    public Incident createIncident(Incident incident) {
        return incidentRepository.save(incident);
    }

    public void deleteIncident(String id) {
        incidentRepository.deleteById(id);
    }

    // Other methods for update, search, etc.
}
