package com.girikgarg.uberauthservice.repositories;

import com.girikgarg.uberentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
}
