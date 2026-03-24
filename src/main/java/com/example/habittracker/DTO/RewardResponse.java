package com.example.habittracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponse {
    private String status;
    private String message;
    private Long exchange;
}
