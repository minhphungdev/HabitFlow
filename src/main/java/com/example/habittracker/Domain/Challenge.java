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
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;
    private String title;
    private String description;
    private Long day;
    @Enumerated(EnumType.STRING)
    private Visibility isPublic;
    private Long participantCount;
    private Long creatorId;
    private Long coinEarnExpect;

    @OneToMany(mappedBy = "challenge")
    private List<Habit> habits;

    @OneToMany(mappedBy = "challenge")
    private List<Daily> dailies;

    @OneToMany(mappedBy = "challenge")
    List<UserChallenge> userChallenges;

    public enum Visibility {
        PUBLIC, PRIVATE, PENDING
    }
}
