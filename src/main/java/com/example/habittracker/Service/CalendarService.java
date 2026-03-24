package com.example.habittracker.Service;

import com.example.habittracker.DTO.*;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CalendarService {
    private final UserDailyRepository userDailyRepository;
    private final TodoHistoryRepository todoHistoryRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final HabitHistoryRepository habitHistoryRepository;
    private final TodoRepository todoRepository;
    private final DiaryRepository diaryRepository;

    public CalendarService(UserDailyRepository userDailyRepository, TodoHistoryRepository todoHistoryRepository, DailyHistoryRepository dailyHistoryRepository, HabitHistoryRepository habitHistoryRepository, TodoRepository todoRepository, DiaryRepository diaryRepository) {
        this.userDailyRepository = userDailyRepository;
        this.todoHistoryRepository = todoHistoryRepository;
        this.dailyHistoryRepository = dailyHistoryRepository;
        this.habitHistoryRepository = habitHistoryRepository;
        this.todoRepository = todoRepository;
        this.diaryRepository = diaryRepository;
    }
    @Transactional
    public CalendarDTO calendarResponse(User user, String date) {

        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<HabitHistory> habitHis = habitHistoryRepository.findCompletedHabitHisByUserAndDate(user, selectedDate);
        List<Long> dailyIds = dailyHistoryRepository.findCompletedDailyIdsByUserAndDate(user, selectedDate);
        List<Long> todoIds = todoHistoryRepository.findCompletedTodoIdsByUserAndDate(user, selectedDate);

        List<UserDaily> dailies;
        List<Todo> todos;
        List<DailyDTO> completedDailies = new ArrayList<>();
        List<HabitDTO> completedHabits = new ArrayList<>();
        List<TodoDTO> completedTodos = new ArrayList<>();

        if (!habitHis.isEmpty()) {

            habitHis.forEach(habit -> {completedHabits.add(HabitDTO.builder()
                    .title(habit.getUserHabit().getHabit().getTitle())
                    .negativeCount(habit.getNegativeCount())
                    .positiveCount(habit.getPositiveCount())
                    .type(habit.getUserHabit().getHabit().getType())
                    .build());});
        }

        if (!dailyIds.isEmpty()) {
            dailies = userDailyRepository.findAllById(dailyIds);
            dailies.forEach(daily -> {completedDailies.add(DailyDTO.builder()
                    .title(daily.getDaily().getTitle())
                    .build());});
        }

        if (!todoIds.isEmpty()) {
            todos = todoRepository.findAllById(todoIds);
            todos.forEach(todo -> {completedTodos.add(TodoDTO.builder()
                    .title(todo.getTitle())
                    .build());});
        }

        List<Long> diaryIds = diaryRepository.findIdByUserAndDate(user, selectedDate);

        return CalendarDTO.builder()
                .selectedDate(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .completedDaily(completedDailies)
                .completedHabits(completedHabits)
                .completedTodos(completedTodos)
                .diaryIds(diaryIds)
                .build();
    }
}
