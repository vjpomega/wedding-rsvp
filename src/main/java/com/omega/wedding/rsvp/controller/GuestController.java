package com.omega.wedding.rsvp.controller;

import com.omega.wedding.rsvp.dto.GuestRsvpRequest;
import com.omega.wedding.rsvp.service.GoogleSheetsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/rsvp")
@CrossOrigin
public class GuestController {


    private final GoogleSheetsService sheetsService;

    public GuestController(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    @PostMapping
    public ResponseEntity<String> submit(@Valid @RequestBody GuestRsvpRequest request) {
        try {
            sheetsService.appendRsvp(request);
            return ResponseEntity.ok("RSVP submitted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit RSVP: " + e.getMessage());
        }
    }
}