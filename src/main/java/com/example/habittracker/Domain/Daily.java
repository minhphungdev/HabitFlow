package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Daily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DailyId;
    private String title;
    private String description;
    private LocalDate createAt = LocalDate.now();
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    @Enumerated(EnumType.STRING)
    private RepeatFrequency repeatFrequency;
    private Integer repeatEvery;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @OneToMany(mappedBy = "daily")
    private List<UserDaily> userDailies;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum RepeatFrequency {
        DAILY, WEEKLY, MONTHLY
    }
}
