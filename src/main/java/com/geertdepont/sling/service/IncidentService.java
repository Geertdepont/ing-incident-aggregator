package com.geertdepont.sling.service;

import com.geertdepont.sling.model.Incident;
import com.geertdepont.sling.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class IncidentService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
    public static final String COULD_NOT_PARSE_HEADER = "Could not parse the header of the csv";
    public static final String COULD_NOT_PARSE_CSV = "Could not parse csv";
    public static final int ASSET_NAME_INDEX = 0;
    public static final int START_DATE_INDEX = 1;
    public static final int END_TIME_INDEX = 2;
    public static final int SEVERITY_INDEX = 3;

    private final IncidentRepository incidentRepository;

    @Autowired
    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void uploadCSV(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try (InputStream inputStream = file.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line = bufferedReader.readLine();
            processHeader(line);

            while ((line = bufferedReader.readLine()) != null) {
                processRow(line);
            }
        }
    }

    private void processHeader(String line) {
        if (line == null) {
            throw new IllegalArgumentException(COULD_NOT_PARSE_HEADER);
        }

        String[] header = line.split(",");

        //TODO For now only check if there are 4 columns (later check if these are Asset Name, Start Date, End Time, Severity)
        if (header.length < 4) {
            throw new IllegalArgumentException(COULD_NOT_PARSE_HEADER);
        }
    }

    private void processRow(String line) throws IllegalArgumentException {
        String[] assetDate = line.split(",");

        if (assetDate.length < 4) {
            throw new IllegalArgumentException(COULD_NOT_PARSE_CSV);
        }

        String assetName = assetDate[ASSET_NAME_INDEX];
        LocalDateTime startTime = LocalDateTime.parse(assetDate[START_DATE_INDEX], DATE_TIME_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(assetDate[END_TIME_INDEX], DATE_TIME_FORMATTER);
        int severity = Integer.parseInt(assetDate[SEVERITY_INDEX]);

        Incident incident = new Incident(
                new Incident.IncidentPrimaryKey(
                        assetName,
                        startTime
                ),
                false,
                endTime,
                severity
        );
        incidentRepository.save(incident);
    }

    public int getRating(int severity) {

        return switch (severity) {
            case 1 -> 30;
            case 2, 3 -> 10;
            default -> 0;
        };
    }
}