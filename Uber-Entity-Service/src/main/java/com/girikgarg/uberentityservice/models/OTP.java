package com.girikgarg.uberentityservice.models;

import jakarta.persistence.Entity;
import lombok.*;
import java.util.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OTP extends BaseModel {
    private String code;

    private String sentToNumber;

    public static OTP make(String phoneNumber) {
        Random random = new Random();

        // generate a random integer from 0 to 8999, then add 1000. This generates four digit OTP
        Integer code = random.nextInt(9000) + 1000;

        return OTP.builder().code(code.toString()).sentToNumber(phoneNumber).build();
    }
}
