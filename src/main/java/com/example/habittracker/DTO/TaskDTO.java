package com.example.habittracker.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    private Long id;
    private String title;
}
