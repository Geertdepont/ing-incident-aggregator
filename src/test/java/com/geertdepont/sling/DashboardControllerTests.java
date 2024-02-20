package com.geertdepont.sling;

import com.geertdepont.sling.repository.DailyDowntimeRepository;
import com.geertdepont.sling.repository.IncidentRepository;
import com.geertdepont.sling.service.AggregateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    AggregateService aggregateIncidentsService;

    @Autowired
    IncidentRepository incidentRepository;

    @Autowired
    DailyDowntimeRepository dailyDowntimeRepository;

    @AfterEach
    void tearDown() {
        incidentRepository.deleteAll();
        dailyDowntimeRepository.deleteAll();
    }

    @Test
    public void dashboardWithValidDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard")
                        .param("date", "2023-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    public void dashboardWithInvalidDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard")
                        .param("date", "invalid_date_format"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid date: Date format must be YYYY-MM-DD"));
    }

    @Test
    public void testOneDayCSV() throws Exception {
        MockMultipartFile file = createMockMultipartFileFromResource("/testfiles/one_day.csv");

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"Created\"}"));

        aggregateIncidentsService.aggregate();


        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard")
                        .param("date", "2019-04-05"))
                .andExpect(request().asyncStarted())
                .andDo(MvcResult::getAsyncResult) // fetch async result similar to "asyncDispatch" builder
                .andExpect(status().isOk())
                .andExpect(content().string("Asset Name, Total Incidents, Total Downtime, Rating\n" +
                        "CRM, 6, 18.2639%, 160\n"));
    }

    @Test
    public void testSimpleSum() throws Exception {
        MockMultipartFile file = createMockMultipartFileFromResource("/testfiles/simple_sum.csv");

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"Created\"}"));

        aggregateIncidentsService.aggregate();


        //simple_sum.csv has two rows: one row with severity 1 and one row with severity 2
        //So downtime will be 21 minutes = 1260 seconds
        //A day has 86.400 seconds. So (1260 / 8640) * 100 = 1.4583% of downtime on that day
        //Rating = (1 * 30) + (1 * 10) = 40

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard")
                        .param("date", "2019-04-01"))
                .andExpect(request().asyncStarted())
                .andDo(MvcResult::getAsyncResult) // fetch async result similar to "asyncDispatch" builder
                .andExpect(status().isOk())
                .andExpect(content().string("Asset Name, Total Incidents, Total Downtime, Rating\n" +
                        "Payments Gateway, 2, 1.4583%, 40\n"));
    }

    @Test
    public void testMultipleDays() throws Exception {
        MockMultipartFile file = createMockMultipartFileFromResource("/testfiles/multiple_days.csv");

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"Created\"}"));

        aggregateIncidentsService.aggregate();

        //Since the input is spread over multiple days
        //we can get the csv for 4/1/2019
        //and for 4/2/2019
        //These are two rows, with both severity 1 and both have 21 minutes, so percentage will be the same as above

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard")
                        .param("date", "2019-04-01"))
                .andExpect(request().asyncStarted())
                .andDo(MvcResult::getAsyncResult) // fetch async result similar to "asyncDispatch" builder
                .andExpect(status().isOk())
                .andExpect(content().string("Asset Name, Total Incidents, Total Downtime, Rating\n" +
                        "CRM, 1, 1.4583%, 30\n"));
    }

    @Test
    public void testAcceptsOnlyHeaderCSV() throws Exception {
        MockMultipartFile file = createMockMultipartFileFromResource("/testfiles/only_header.csv");

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"Created\"}"));
    }

    private MockMultipartFile createMockMultipartFileFromResource(String resourceName) throws IOException {
        Path resourcePath = new ClassPathResource(resourceName).getFile().toPath();
        String originalFileName = resourcePath.getFileName().toString();
        String contentType = Files.probeContentType(resourcePath);
        byte[] content = Files.readAllBytes(resourcePath);

        return new MockMultipartFile("file", originalFileName, contentType, content);
    }

}