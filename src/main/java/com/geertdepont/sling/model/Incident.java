package com.geertdepont.sling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.LocalDateTime;


@Table
@Data
@AllArgsConstructor
public class Incident {

    @PrimaryKeyClass
    @Data
    @AllArgsConstructor
    public static class IncidentPrimaryKey {

        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String assetName;

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private LocalDateTime startDate;
    }

    @PrimaryKey
    private IncidentPrimaryKey primaryKey;

    @Indexed
    private boolean processed;
    private LocalDateTime endDate;
    private int severity;
}