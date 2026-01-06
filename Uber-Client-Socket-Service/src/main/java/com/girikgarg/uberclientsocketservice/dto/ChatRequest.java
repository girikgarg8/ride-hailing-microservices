package com.girikgarg.uberclientsocketservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    private String name;
    private String message;
}

