package com.geertdepont.sling.service;

import com.geertdepont.sling.model.DailyDowntime;
import com.geertdepont.sling.repository.DailyDowntimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Service
public class CSVExportService {

    private final DailyDowntimeRepository dailyDowntimeRepository;

    public static final String CSV_OUTPUT_HEADER = "Asset Name, Total Incidents, Total Downtime, Rating";

    @Autowired
    public CSVExportService(DailyDowntimeRepository dailyDowntimeRepository) {
        this.dailyDowntimeRepository = dailyDowntimeRepository;
    }

    public StreamingResponseBody streamCSV(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        final LocalDate finalDate = date;
        return outputStream -> {
            try (PrintWriter writer = new PrintWriter(outputStream)) {
                List<DailyDowntime> dailyDowntimes = dailyDowntimeRepository.findByDate(finalDate);

                writer.println(CSV_OUTPUT_HEADER);

                for (DailyDowntime downtime : dailyDowntimes) {
                    writer.println(
                            downtime.getPrimaryKey().getAssetName() + ", " +
                            downtime.getTotalIncidents() + ", " +
                            Math.round(downtime.getDownTimePercentage() * 10000.0) / 10000.0 + "%" + ", " +
                            downtime.getRating()
                    );
                }
            }
        };
    }

}
