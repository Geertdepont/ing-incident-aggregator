package com.geertdepont.sling.repository;

import com.geertdepont.sling.model.DailyDowntime;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyDowntimeRepository extends CrudRepository<DailyDowntime, DailyDowntime.CompositeKey> {
    @Query("SELECT * FROM dailydowntime WHERE date = ?0")
    List<DailyDowntime> findByDate(LocalDate date);
}