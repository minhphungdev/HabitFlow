package com.example.habittracker.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoSubtaskDTO {
    private Long todoSubtaskId;
    private String title;
    private boolean isCompleted;

}
