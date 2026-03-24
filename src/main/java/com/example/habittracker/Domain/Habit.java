package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long habitId;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private HabitType type;
    private LocalDateTime createAt = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    private Long targetCount;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @OneToMany(mappedBy = "habit")
    private List<UserHabit> userHabits;

    public enum HabitType {
        POSITIVE, NEGATIVE, BOTH
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
