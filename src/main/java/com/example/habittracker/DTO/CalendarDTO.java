package com.example.habittracker.DTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarDTO {
    private String selectedDate;
    private List<DailyDTO> completedDaily;
    private List<HabitDTO> completedHabits;
    private List<TodoDTO> completedTodos;
    private List<Long> diaryIds;
}
