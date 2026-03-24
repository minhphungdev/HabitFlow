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
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;
    private LocalDate date;
    private String content;
    private String imageUrl;
    private Long challengeId;

    @ManyToOne()
    @JoinColumn(name="user_id")
    private User user;

    @ManyToMany
    @JoinTable(name = "diary_user_habit",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "user_habit_id"))
    private List<UserHabit> userHabitList;

    @ManyToMany
    @JoinTable(name = "diary_user_daily",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "user_daily_id"))
    private List<UserDaily> userDailyList;

    @ManyToMany
    @JoinTable(name = "diary_todo",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "todo_id"))
    private List<Todo> todoList;

}
