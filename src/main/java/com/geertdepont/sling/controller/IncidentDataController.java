package com.geertdepont.sling.controller;

import com.geertdepont.sling.dto.DashboardRequest;
import com.geertdepont.sling.dto.Response;
import com.geertdepont.sling.service.CSVExportService;
import com.geertdepont.sling.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Validated
public class IncidentDataController {

    private final IncidentService incidentService;

    private final CSVExportService csvExportService;

    public static final String OUTPUT_FILENAME = "output.csv";

    @Autowired
    public IncidentDataController( IncidentService incidentService, CSVExportService csvExportService) {
        this.incidentService = incidentService;
        this.csvExportService = csvExportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StreamingResponseBody> dashboard(@Valid DashboardRequest request) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", OUTPUT_FILENAME);
        StreamingResponseBody responseBody = csvExportService.streamCSV(request.getDate());

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        incidentService.uploadCSV(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(new Response("Created"));
    }
}