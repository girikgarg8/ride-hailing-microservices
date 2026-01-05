package com.girikgarg.uberentityservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Color extends BaseModel {
    
    @Column(unique = true, nullable = false)
    private String name;  // e.g., White, Black, Silver
}
