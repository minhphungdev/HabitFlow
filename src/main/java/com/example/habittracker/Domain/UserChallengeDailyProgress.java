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
public class UserChallengeDailyProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Integer completionPercentage;

    @ManyToOne
    @JoinColumn(name = "user_challenge_id")
    private UserChallenge userChallenge;
}
