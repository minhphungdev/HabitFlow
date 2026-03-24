package com.example.habittracker.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDTO {
    private String userName;
    private String userAvatar;
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
}
