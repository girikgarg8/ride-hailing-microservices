package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "db_constant", indexes = {
    @Index(name = "idx_db_constant_name", columnList = "name")
})
public class DBConstant extends BaseModel {
    
    @Column(unique = true, nullable = false)
    private String name;  // Configuration key (e.g., BASE_FARE, PER_KM_RATE)

    @Column(columnDefinition = "TEXT")
    private String value;  // Configuration value
}
