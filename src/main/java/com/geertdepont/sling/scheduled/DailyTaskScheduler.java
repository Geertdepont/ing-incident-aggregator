package com.geertdepont.sling.scheduled;

import com.geertdepont.sling.service.AggregateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class DailyTaskScheduler {
    private final AggregateService aggregateIncidentService;

    @Autowired
    public DailyTaskScheduler(AggregateService aggregateIncidentService) {
        this.aggregateIncidentService = aggregateIncidentService;
    }

    // Schedule the task to run at 04:00 AM
    @Scheduled(cron = "0 0 4 * * *")
    public void executeDailyTask() {
        aggregateIncidentService.aggregate();
    }
}
