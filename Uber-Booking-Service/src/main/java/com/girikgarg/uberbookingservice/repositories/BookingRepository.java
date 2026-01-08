package com.girikgarg.uberbookingservice.repositories;

import com.girikgarg.uberentityservice.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Standard CRUD operations provided by JpaRepository
    // Custom update is handled in service layer using findById() + save()
}
