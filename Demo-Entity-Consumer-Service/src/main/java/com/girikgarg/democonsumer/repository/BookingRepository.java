package com.girikgarg.democonsumer.repository;

import com.girikgarg.uberentityservice.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // ⭐ Successfully using Booking entity from imported Entity Service!
}

