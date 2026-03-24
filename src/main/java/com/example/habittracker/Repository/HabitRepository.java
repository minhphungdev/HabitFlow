package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Habit;
import com.example.habittracker.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    @Query("SELECT uh.habit FROM UserHabit uh WHERE uh.user.userId = :userId ")
    List<Habit> findAllHabitsForUser(@Param("userId")Long userId);

    @Query("SELECT h FROM Habit h WHERE h.challenge.challengeId = :challengeId")
    List<Habit> findByChallenge(@Param("challengeId")Long challengeId);

}
