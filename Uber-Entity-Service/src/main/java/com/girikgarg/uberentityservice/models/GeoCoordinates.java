package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GeoCoordinates extends BaseModel {
    private Double latitude;
    private Double longitude;
}
