package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
// where driver reviews the booking
public class PassengerReview extends Review {
    @Column(nullable = false)
    private String passengerReviewCount;

    @Column(nullable = false)
    private String passengerRating;
}
