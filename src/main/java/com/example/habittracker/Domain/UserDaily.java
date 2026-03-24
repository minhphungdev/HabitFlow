package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDailyId;
    private Long streak = 0L;
    private boolean isCompleted;
    private boolean isEnabled;
    private Integer repeatEvery;

    private boolean unavailable = false;

    private boolean isInChallenge = false;

    @Enumerated(EnumType.STRING)
    private Daily.RepeatFrequency repeatFrequency;

    @Enumerated(EnumType.STRING)
    private Daily.Difficulty difficulty;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne()
    @JoinColumn(name = "daily_id")
    private Daily daily;

    @OneToMany(mappedBy = "userDaily")
    private List<DailyHistory> dailyHistories;

    @ElementCollection
    @CollectionTable(name = "user_daily_repeat_days", joinColumns = @JoinColumn(name = "user_daily_id"))
    @Column(name = "day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> repeatDays = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_daily_repeat_month_days", joinColumns = @JoinColumn(name = "user_daily_id"))
    @Column(name = "day_of_month")
    private Set<Integer> repeatMonthDays = new HashSet<>();


    public enum DayOfWeek {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }
}
