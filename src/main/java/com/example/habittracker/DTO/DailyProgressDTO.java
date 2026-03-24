package com.example.habittracker.DTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProgressDTO {
    private LocalDate date;
    private Integer completionPercentage;
}
