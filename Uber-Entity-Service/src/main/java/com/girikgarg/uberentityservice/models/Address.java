package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter


public class Address extends BaseModel { // like "Shanti Park"
    @OneToOne
    private GeoCoordinates geoCoordinates;

    private String name;

    private String zipCode;

    private String city;

    private String country;

    private String state;
}
