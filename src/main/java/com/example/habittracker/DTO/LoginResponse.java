package com.example.habittracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private List<ChallengeDTO> challengesCompleted;
    private List<AchievementDTO> achievementsCompleted;
}
