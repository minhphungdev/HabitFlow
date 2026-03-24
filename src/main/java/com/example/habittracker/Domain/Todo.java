package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long TodoId;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    private LocalDate execution_date;
    private LocalDate created_at = LocalDate.now();
    private boolean isCompleted = false;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "todo")
    private List<TodoSubtask> todoSubTasks;

    @OneToMany(mappedBy = "todo")
    private List<TodoHistory> todoHistories;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public boolean isAllSubtasksCompleted() {
        return todoSubTasks.stream().allMatch(TodoSubtask::isCompleted);
    }
}
