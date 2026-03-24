package com.example.habittracker.DTO;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AchievementDTO {
    private Long achievementId;
    private String achievementTitle;
    private String achievementDescription;
    private Long bonusChallenge;
    private Long bonusTask;
    private Long requiredChallenge;
    private Long requiredTask;
    private String color;
    private String icon;
    private Long coinBonus;
    private Long durationAchievement;
    private UserDTO user;
}
