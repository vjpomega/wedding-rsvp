package com.omega.wedding.rsvp.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.omega.wedding.rsvp.dto.GuestRsvpRequest;
import org.springframework.beans.factory.annotation.Value;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private static final String APPLICATION_NAME = "Wedding RSVP";
    private static final String RANGE = "Sheet1!A:E"; // Adjust based on your sheet structure

    public void appendRsvp(GuestRsvpRequest request) throws Exception {
        Sheets service = getSheetsService();

        // Check if email already exists
        if (emailExists(service, request.email())) {
            throw new RuntimeException("RSVP already submitted with this email");
        }

        // Prepare the row data
        List<Object> row = Arrays.asList(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                request.fullName(),
                request.email(),
                request.attending() ? "Yes" : "No"
        );

        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(row));

        AppendValuesResponse result = service.spreadsheets().values()
                .append(spreadsheetId, RANGE, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.printf("%d cells appended.%n", result.getUpdates().getUpdatedCells());
    }

    public List<List<Object>> getAllRsvps() throws Exception {
        Sheets service = getSheetsService();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, RANGE)
                .execute();

        return response.getValues();
    }

    private boolean emailExists(Sheets service, String email) throws IOException, IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, RANGE)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return false;
        }

        // Skip header row (index 0) and check email column (index 2)
        return values.stream()
                .skip(1)
                .anyMatch(row -> row.size() > 2 && row.get(2).toString().equalsIgnoreCase(email));
    }

    private Sheets getSheetsService() throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
