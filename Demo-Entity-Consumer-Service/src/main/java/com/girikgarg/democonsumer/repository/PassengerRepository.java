package com.girikgarg.democonsumer.repository;

import com.girikgarg.uberentityservice.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    // ⭐ Successfully using Passenger entity from imported Entity Service!
}

