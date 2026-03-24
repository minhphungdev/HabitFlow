package com.example.habittracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RewardDTO {
    private Long rewardId;
    private String title;
    private String description;
    private Long coinCost = 0L;
}
