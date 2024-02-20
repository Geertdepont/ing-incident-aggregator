package com.geertdepont.sling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyDowntime {

    public static final int AMOUNT_OF_SECONDS_IN_DAY = 60 * 60 * 24;

    @PrimaryKeyClass
    @Data
    @AllArgsConstructor
    public static class CompositeKey implements Serializable {
        @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private LocalDate date;

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private String assetName;
    }

    @PrimaryKey
    private CompositeKey primaryKey;

    private int totalIncidents;

    private int totalDowntimeSeconds;

    private int rating;

    public double getDownTimePercentage() {
        return ((double) getTotalDowntimeSeconds() / AMOUNT_OF_SECONDS_IN_DAY) * 100;
    }
}


