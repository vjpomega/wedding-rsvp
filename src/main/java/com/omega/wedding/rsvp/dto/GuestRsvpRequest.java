package com.omega.wedding.rsvp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GuestRsvpRequest(@NotBlank String fullName,
                               @NotBlank @Email String email,
                               boolean attending) {}
