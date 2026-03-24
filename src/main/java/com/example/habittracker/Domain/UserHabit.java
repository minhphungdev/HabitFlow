package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHabit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userHabitId;
    private Long targetCount;
    private String emailMessage;
    private LocalDateTime timeSendEmail;
    private Long negativeCount = 0L;
    private Long positiveCount = 0L;
    private boolean isCompleted;

    private boolean unavailable = false;

    private boolean isInChallenge = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @OneToMany(mappedBy = "userHabit")
    private List<HabitHistory> habitHistories;

    @Enumerated(EnumType.STRING)
    private Habit.Difficulty difficulty;
}
