package com.berkeley.irms.warnme.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.berkeley.irms.warnme.cache.InMemCache;
import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.repositories.IncidentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    public List<Incident> getIncidents(String type, String severity, String source, String dateFrom, String dateTo) {
        Stream<Incident> incidentStream = getAllIncidents().stream();

        if (type != null && !type.isBlank()) {
            incidentStream = incidentStream.filter(incident ->
                    type.equalsIgnoreCase(incident.getIncidentType()) || type.equalsIgnoreCase(incident.getType()));
        }

        if (severity != null && !severity.isBlank()) {
            incidentStream = incidentStream.filter(incident -> severity.equalsIgnoreCase(incident.getSeverity()));
        }

        if (source != null && !source.isBlank()) {
            incidentStream = incidentStream.filter(incident -> source.equalsIgnoreCase(incident.getSource()));
        }

        if (dateFrom != null && !dateFrom.isBlank()) {
            incidentStream = incidentStream.filter(incident ->
                    incident.getIncidentDate() == null || incident.getIncidentDate().compareTo(dateFrom) >= 0);
        }

        if (dateTo != null && !dateTo.isBlank()) {
            incidentStream = incidentStream.filter(incident ->
                    incident.getIncidentDate() == null || incident.getIncidentDate().compareTo(dateTo) <= 0);
        }

        return incidentStream.toList();
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
        cache.clear();
        return incidentRepository.saveAll(incidents);  // Save a list of incidents
    }

    public boolean existsByGmailMessageId(String gmailMessageId) {
        return gmailMessageId != null
                && !gmailMessageId.isBlank()
                && incidentRepository.existsBySourceMetadataGmailMessageId(gmailMessageId);
    }

    public Optional<Incident> findOpenWarnMeIncident(String incidentType, String locationText) {
        if (incidentType == null || incidentType.isBlank()) {
            return Optional.empty();
        }

        String normalizedLocation = locationText == null ? "" : locationText.toLowerCase();
        return incidentRepository.findBySourceAndIncidentType("gmail_warnme", incidentType)
                .stream()
                .filter(incident -> !"closed".equalsIgnoreCase(incident.getAlertStatus()))
                .filter(incident -> normalizedLocation.isBlank()
                        || incident.getLocationText() == null
                        || normalizedLocation.contains(incident.getLocationText().toLowerCase())
                        || incident.getLocationText().toLowerCase().contains(normalizedLocation))
                .findFirst();
    }

    public void deleteIncident(String id) {
        cache.clear();
        incidentRepository.deleteById(id);
    }
}
