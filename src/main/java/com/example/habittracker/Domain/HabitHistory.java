package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HabitHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long HistoryHabitId;
    private LocalDate date;
    private boolean isCompleted;
    private Long negativeCount;
    private Long positiveCount;
    private Long coinEarned;

    @ManyToOne
    @JoinColumn(name = "user_habit_id")
    private UserHabit userHabit;
}
