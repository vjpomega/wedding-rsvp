package com.omega.wedding.rsvp.controller;

import com.omega.wedding.rsvp.dto.GuestRsvpRequest;
import com.omega.wedding.rsvp.entity.Guest;
import com.omega.wedding.rsvp.repository.GuestRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rsvp")
@CrossOrigin
public class GuestController {

    private final GuestRepository repository;

    public GuestController(GuestRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Guest submit(@Valid @RequestBody GuestRsvpRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new RuntimeException("RSVP already submitted with this email");
        }

        Guest guest = new Guest();
        guest.setFullName(request.fullName());
        guest.setEmail(request.email());
        guest.setAttending(request.attending());
        return repository.save(guest);
    }

    @GetMapping
    public List<Guest> all() {
        return repository.findAll();
    }
}