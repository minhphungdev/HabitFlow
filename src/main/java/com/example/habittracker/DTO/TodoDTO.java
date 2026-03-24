package com.example.habittracker.DTO;

import com.example.habittracker.Domain.Todo;
import com.example.habittracker.Domain.TodoSubtask;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoDTO {
    private Long todoId;
    private String title;
    private String description;
    private Todo.Difficulty difficulty;
    private LocalDate executionDate;
    private List<String> subtasks;
    private List<Long> subtaskIds;
    private List<TodoSubtaskDTO> todoSubtasks;
    private boolean isCompleted;
    private String userCoinMessage;
    private Long coinEarned;

    private final Todo.Difficulty[] todoDifficultOption = Todo.Difficulty.values();
}
