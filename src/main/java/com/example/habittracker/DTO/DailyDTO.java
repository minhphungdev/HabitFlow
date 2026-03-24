package com.example.habittracker.DTO;

import com.example.habittracker.Domain.Daily;
import com.example.habittracker.Domain.UserDaily;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyDTO {
    private Long dailyId;
    private Long userId;
    private Long challengeId;
    private String challengeTitle;
    private String title;
    private String description;
    private Daily.Difficulty difficulty;
    private Daily.RepeatFrequency repeatFrequency;
    private Integer repeatEvery;
    private Set<UserDaily.DayOfWeek> repeatDays = new HashSet<>();
    private Set<Integer> repeatMonthDays = new HashSet<>();
    private Long streak;
    private boolean isCompleted;
    private boolean isPublic;
    private boolean isInChallenge;
    private String userCoinMessage;
    private Long coinEarned;

    private final Daily.Difficulty[] difficultyOptions = Daily.Difficulty.values();
    private final Daily.RepeatFrequency[] repeatFrequencyOptions = Daily.RepeatFrequency.values();
    private final UserDaily.DayOfWeek[] dayOfWeekOptions = UserDaily.DayOfWeek.values();
}
