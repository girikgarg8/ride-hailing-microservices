package com.girikgarg.uberreviewservice.repositories;

import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId")
    Review findReviewByBookingId(Long bookingId);
}

