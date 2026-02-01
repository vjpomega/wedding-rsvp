package com.omega.wedding.rsvp.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.omega.wedding.rsvp.dto.GuestRsvpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleSheetsService {

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    private static final String APPLICATION_NAME = "Wedding RSVP";
    private static final String RANGE = "Sheet1!A:D";

    public void appendRsvp(GuestRsvpRequest request) throws Exception {
        Sheets service = getSheetsService();

        if (emailExists(service, request.email())) {
            throw new RuntimeException("RSVP already submitted with this email");
        }

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

        System.out.printf("%d cells appended.%n",
                result.getUpdates().getUpdatedCells());
    }

    public List<List<Object>> getAllRsvps() throws Exception {
        Sheets service = getSheetsService();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, RANGE)
                .execute();

        return response.getValues();
    }

    private boolean emailExists(Sheets service, String email) throws Exception {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, RANGE)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.size() <= 1) {
            return false;
        }

        return values.stream()
                .skip(1) // skip header
                .anyMatch(row ->
                        row.size() > 2 &&
                                row.get(2).toString().equalsIgnoreCase(email)
                );
    }

    private Sheets getSheetsService() throws Exception {

        String credentialsJson =
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");

        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException(
                    "Missing GOOGLE_APPLICATION_CREDENTIALS_JSON env variable"
            );
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(
                        credentialsJson.getBytes(StandardCharsets.UTF_8)))
                .createScoped(
                        List.of("https://www.googleapis.com/auth/spreadsheets")
                );

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
