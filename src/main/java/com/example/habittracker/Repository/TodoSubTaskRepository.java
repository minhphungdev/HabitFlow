package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Todo;
import com.example.habittracker.Domain.TodoSubtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TodoSubTaskRepository extends JpaRepository<TodoSubtask,Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM TodoSubtask ts WHERE ts.todo = :todo")
    void deleteAllByTodo(@Param("todo") Todo todo);
}
