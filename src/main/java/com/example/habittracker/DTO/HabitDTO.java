package com.example.habittracker.DTO;

import com.example.habittracker.Domain.Habit;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HabitDTO {
    private Long habitId;
    private String title;
    private String description;
    private Habit.Difficulty difficulty;
    private Habit.HabitType type;
    private Long challengeId;
    private Long targetCount =0L;
    private Long userId;
    private Long negativeCount;
    private Long positiveCount;
    private boolean isCompleted =false;
    private boolean isPublic;
    private boolean isInChallenge;
    private String userCoinMessage;
    private Long coinEarned;

    private final Habit.Difficulty[] habitDifficultiesOption = Habit.Difficulty.values();
    private final Habit.HabitType[] habitTypesOption = Habit.HabitType.values();
}
