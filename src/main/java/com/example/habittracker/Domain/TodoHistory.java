package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoHistoryId;
    private LocalDate date;
    private boolean isCompleted;
    private Long coinEarned = 0L;

    @ManyToOne()
    @JoinColumn(name = "todo_id")
    private Todo todo;
}
