package com.geertdepont.sling.repository;

import com.geertdepont.sling.model.Incident;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

public interface IncidentRepository extends CrudRepository<Incident, Incident.IncidentPrimaryKey> {
    @Query("SELECT * FROM Incident WHERE processed = false")
    Slice<Incident> findUnprocessedIncidents(Pageable pageSize);
}