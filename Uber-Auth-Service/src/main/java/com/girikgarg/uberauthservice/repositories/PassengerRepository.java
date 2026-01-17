package com.girikgarg.uberauthservice.repositories;

import com.girikgarg.uberentityservice.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByEmail(String email);
}
