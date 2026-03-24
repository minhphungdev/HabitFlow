package com.example.habittracker.DTO;

import com.example.habittracker.Domain.Challenge;
import com.example.habittracker.Domain.UserChallenge;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeDTO {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDate endDate;
    private LocalDate startDate;
    private Long day;
    private Challenge.Visibility isPublic;
    private List<HabitDTO> habits;
    private List<DailyDTO> dailies;
    private UserChallenge.Status status;
    private Long challengeParticipant;

    private Double progress;
    private Long bestStreak;
    private Long totalCompletedTasks;
    private Long totalExpectedTasks;
    private Long completedTasks;
    private Long skippedTasks;
    private List<LocalDate> completedTasksList;
    private List<DailyProgressDTO> dailyProgresses;
    private Long coinEarn;
    private double evaluationPercentage;
}
