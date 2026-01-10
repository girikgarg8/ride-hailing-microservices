package com.girikgarg.ubersocketservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String name;
    private String message;
    private String timestamp;
}

