package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Car extends BaseModel {
    
    @Column(nullable = false, unique = true)
    private String registrationNumber;  // License plate number
    
    @Column(nullable = false)
    private String brand;  // e.g., Toyota, Honda, Tesla
    
    @Column(nullable = false)
    private String model;  // e.g., Camry, Accord, Model 3

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarType carType;

    @OneToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
}
