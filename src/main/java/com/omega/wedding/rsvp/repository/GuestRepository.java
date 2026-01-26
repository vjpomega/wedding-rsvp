package com.omega.wedding.rsvp.repository;

import com.omega.wedding.rsvp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    boolean existsByEmail(String email);
}
