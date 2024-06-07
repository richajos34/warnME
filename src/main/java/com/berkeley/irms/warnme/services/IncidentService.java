package com.berkeley.irms.warnme.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.berkeley.irms.warnme.cache.InMemCache;
import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.repositories.IncidentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private static final String incidentsCacheKey = "incidentsCacheKey";
    private final IncidentRepository incidentRepository;
    private final InMemCache<List<Incident>> cache = new InMemCache<>();;

    @Autowired
    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> getAllIncidents() {
        List<Incident> incidents = null;
        incidents = cache.get(incidentsCacheKey);
        if (incidents != null) {
            return incidents;
        }

        incidents = incidentRepository.findAll();
        if (incidents != null && incidents.size() > 0) {
            cache.put(incidentsCacheKey, incidents);
        }
        return incidents;
    }

    public Optional<Incident> getIncidentById(String id) {
        return incidentRepository.findById(id);
    }

    public Optional<Incident> getIncidentByTitle(String title) {
        return incidentRepository.findFirstByTitleContainingIgnoreCase(title);
    }

    public Incident createIncident(Incident incident) {
        cache.clear();
        return incidentRepository.save(incident);
    }

    public List<Incident> saveAllIncidents(List<Incident> incidents) {
        return incidentRepository.saveAll(incidents);  // Save a list of incidents
    }

    public void deleteIncident(String id) {
        cache.clear();
        incidentRepository.deleteById(id);
    }
}
