package com.geertdepont.sling.service;

import com.geertdepont.sling.model.DailyDowntime;
import com.geertdepont.sling.model.Incident;
import com.geertdepont.sling.repository.DailyDowntimeRepository;
import com.geertdepont.sling.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AggregateService {

    private final IncidentRepository incidentRepository;

    private final DailyDowntimeRepository dailyDowntimeRepository;

    private final IncidentService incidentService;

    @Value("${spring.data.cassandra.request.page-size}")
    private int cassandraPageSize;

    @Autowired
    public AggregateService(IncidentRepository incidentRepository, DailyDowntimeRepository dailyDowntimeRepository, IncidentService incidentService) {
        this.incidentRepository = incidentRepository;
        this.dailyDowntimeRepository = dailyDowntimeRepository;
        this.incidentService = incidentService;
    }

    /**
     * Go over all the incidents, aggregate the incidents based on date and assetName.
     * Using pagination to not load everything directly in to memory
     * TODO when new incidents get uploaded the database, and if they contain the same date, old downtime will be replaced with new one
     * TODO if that downtime already exists then
     */
    public void aggregate() {
        Slice<Incident> unprocessedIncidents = incidentRepository.findUnprocessedIncidents(CassandraPageRequest.first(cassandraPageSize));
        List<Incident> incidents;

        DailyDowntime downtime = new DailyDowntime();

        while(unprocessedIncidents.hasNext()) {
            //Since the data is sorted, we can just loop over the data and if the data is different we store the aggregated object
            incidents = unprocessedIncidents.getContent();
            downtime = aggregateIncidents(incidents, downtime);
            unprocessedIncidents = incidentRepository.findUnprocessedIncidents(unprocessedIncidents.nextPageable());
        }

        //Get last items
        incidents = unprocessedIncidents.getContent();
        aggregateIncidents(incidents, downtime);
    }

    /**
     * Loop over the sorted incidents: If the assetName is different or the date is different, then we save that downtime and create a new downtime
     *
     * @param incidents List<Incident> incidents (Sorted on assetName, then date)
     * @param downtime DailyDowntime object (in which the data of that asset will be stored)
     *
     * @return DailyDowntime object (the aggregated assets)
     */
    private DailyDowntime aggregateIncidents(List<Incident> incidents, DailyDowntime downtime) {
        if (incidents.size() == 0){
            return downtime;
        }

        Incident firstIncident = incidents.getFirst();

        if (downtime.getPrimaryKey() == null) {
            downtime.setPrimaryKey(new DailyDowntime.CompositeKey(firstIncident.getPrimaryKey().getStartDate().toLocalDate(), firstIncident.getPrimaryKey().getAssetName()));
        }

        for (Incident incident: incidents) {
            String incidentAsset = incident.getPrimaryKey().getAssetName();
            LocalDate incidentDate = incident.getPrimaryKey().getStartDate().toLocalDate();
            int downtimeSeconds = incident.getSeverity() == 1 ? (int) (Duration.between(incident.getPrimaryKey().getStartDate(), incident.getEndDate()).toSeconds()) : 0;

            //If this incident is not related to this asset or this date, store the aggregated data and create a new one.
            if (
                !downtime.getPrimaryKey().getAssetName().equals(incidentAsset) ||
                !downtime.getPrimaryKey().getDate().isEqual(incidentDate)
            ) {
                dailyDowntimeRepository.save(downtime);
                downtime = new DailyDowntime(
                    new DailyDowntime.CompositeKey(incidentDate, incidentAsset),
                    1,
                    downtimeSeconds,
                    incidentService.getRating(incident.getSeverity())
                );
            } else {
                downtime.setRating(downtime.getRating() + incidentService.getRating(incident.getSeverity()));
                downtime.setTotalIncidents(downtime.getTotalIncidents() + 1);
                downtime.setTotalDowntimeSeconds(downtime.getTotalDowntimeSeconds() + downtimeSeconds);
            }

            incident.setProcessed(true);
        }

        dailyDowntimeRepository.save(downtime);
        incidentRepository.saveAll(incidents);

        return downtime;
    }
}
