package com.berkeley.irms.warnme.repositories;

import com.berkeley.irms.warnme.models.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {

    List<Incident> findByStatus(String status);

     Optional<Incident> findFirstByTitleContainingIgnoreCase(String keyword);
}