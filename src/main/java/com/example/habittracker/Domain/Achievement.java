package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long achievementId;
    private String title;
    private String description;
    private Long requiredTask;
    private Long requiredChallenge;
    private Long taskBonus;
    private Long challengeBonus;
    private Long coinBonus;
    private boolean isAvailable;
    private String icon;
    private String color;

    @OneToMany(mappedBy = "achievement")
    List<UserAchievement> userAchievements;
}
