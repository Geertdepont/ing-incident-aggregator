package com.geertdepont.sling.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
public class DashboardRequest {
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private String date;

    /**
     * Takes a date string (yyyy-mm-dd) and turns it into a localDate
     * @return LocalDate
     */
    public LocalDate getDate() {
        if (this.date == null) {
            return LocalDate.now();
        }

        try {
            LocalDate parsedDate = LocalDate.parse(this.date, DATE_TIME_FORMATTER);

            if (!parsedDate.toString().equals(this.date)) {
                //In case of 31st of February
                throw new IllegalArgumentException("Invalid date: Date does not exist");
            }

            LocalDate now = LocalDate.now();

            if (parsedDate.isAfter(now)) {
                throw new IllegalArgumentException("Invalid date: Date cannot be in the future");
            }

            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: Date format must be YYYY-MM-DD");
        }
    }
}
