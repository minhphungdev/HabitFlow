package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoSubtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoSubtaskId;
    private String title;
    private boolean isCompleted;

    @ManyToOne()
    @JoinColumn(name = "todo_id")
    private Todo todo;
}
