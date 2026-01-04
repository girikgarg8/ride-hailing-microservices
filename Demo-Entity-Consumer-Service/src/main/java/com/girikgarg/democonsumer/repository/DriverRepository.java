package com.girikgarg.democonsumer.repository;

import com.girikgarg.uberentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    // ⭐ Successfully using Driver entity from imported Entity Service!
}

