package com.example.habittracker.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private String type;
    private String message;
}
