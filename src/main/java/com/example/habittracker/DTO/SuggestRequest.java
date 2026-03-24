package com.example.habittracker.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestRequest {
    private String title;
    private String description;
    private Long durationDay;
    private Long habitSuggestNum;
    private Long dailySuggestNum;
}
