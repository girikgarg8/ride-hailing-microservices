package com.girikgarg.uberentityservice.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bookings", "car"})
public class Driver extends BaseModel {

    private String name;
    
    @Column(nullable = false, unique = true)
    private String email; // For authentication
    
    @Column(nullable = false)
    private String password; // Hashed password for authentication

    private String governmentIdNumber; // e.g., Aadhar, SSN, National ID

    private String phoneNumber;

    // 1 : 1 , Driver : Car
    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private Car car;

    @Enumerated(value = EnumType.STRING)
    private DriverApprovalStatus driverApprovalStatus;

    @OneToOne
    private GeoCoordinates lastKnownLocation;

    @OneToOne
    private GeoCoordinates home; // towards the end of day, the rides will be assigned to driver which are near their home

    private String activeCity;

    @DecimalMin(value = "0.00", message = "Rating must be greater than or equal to 0.00")
    @DecimalMax(value = "5.00", message = "Rating must be less than or equal to 5.00")
    private Double rating;

    private boolean isAvailable;

    // 1 : n , Driver : Booking
    @OneToMany(mappedBy = "driver")
    @Fetch(FetchMode.SUBSELECT)
    private List<Booking> bookings;
}

