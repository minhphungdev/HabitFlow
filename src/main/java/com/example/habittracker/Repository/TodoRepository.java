package com.example.habittracker.Repository;

import com.example.habittracker.Domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Query("SELECT th from Todo th WHERE th.user = :user")
    List<Todo> findByUser(@Param("user") User user);

    @Query("SELECT th FROM Todo th WHERE th.user = :user AND th.isCompleted = false ORDER BY th.TodoId DESC")
    List<Todo> findByUserAndIsCompletedFalse(@Param("user") User user);

    @Query("SELECT t FROM Todo t WHERE t.user = :user and t.TodoId = :todoId")
    Optional<Todo> findByUserAndTodoId(@Param("user") User user, @Param("todoId")Long todoId);

    @Query("SELECT th from Todo th WHERE th.user = :user")
    List<Todo> findAllByUser(@Param("user")User user);
}
