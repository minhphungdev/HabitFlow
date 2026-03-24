package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Todo;
import com.example.habittracker.Domain.TodoHistory;
import com.example.habittracker.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoHistoryRepository extends JpaRepository<TodoHistory,Long> {
    @Query("SELECT th FROM TodoHistory th WHERE th.todo = :todoId AND th.date = :today")
    Optional<TodoHistory> findByDateAndTodoId(@Param("todoId")Todo todo, @Param("today")LocalDate today);

    @Query("SELECT th.todo.TodoId FROM TodoHistory th WHERE th.todo.user = :user AND th.isCompleted = true AND DATE(th.date) = :date")
    List<Long> findCompletedTodoIdsByUserAndDate(@Param("user")User user, @Param("date")LocalDate date);

    @Query("SELECT th.coinEarned FROM TodoHistory th WHERE th.todo = :todo AND th.date=:today")
    Long findCoinEarnedByTodoAndToday(@Param("todo")Todo todo, @Param("today")LocalDate today);

    @Query("SELECT COUNT(*) FROM TodoHistory th WHERE th.todo = :todo AND th.isCompleted = true")
    Long countCompleteTask(@Param("todo")Todo todo);
}
