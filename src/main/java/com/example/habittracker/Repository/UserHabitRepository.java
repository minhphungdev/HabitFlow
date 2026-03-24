package com.example.habittracker.Repository;

import com.example.habittracker.Domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserHabitRepository extends JpaRepository<UserHabit,Long> {
    @Query("SELECT uh FROM UserHabit uh WHERE uh.user.userId = :userId ORDER BY uh.userHabitId DESC")
    List<UserHabit> findHabitsForUser(@Param("userId")Long userId);

    Optional<UserHabit> findUserHabitByHabitAndUser(Habit habit, User user);

    @Query("SELECT uh FROM UserHabit uh WHERE uh.habit.challenge.challengeId = :challenge AND uh.user = :user")
    List<UserHabit> findByUser_Challenge(@Param("user")User user,@Param("challenge")Challenge challenge);

    @Query("SELECT uh FROM UserHabit uh WHERE uh.user =:user AND uh.habit.challenge = :challenge AND uh.unavailable = false ")
    List<UserHabit> findByUserAndHabitChallengeAndUnavailableFalse(@Param("user")User user, @Param("challenge") Challenge challenge);

    @Query("SELECT uh FROM UserHabit uh WHERE uh.user =:user AND uh.habit.challenge =null And uh.unavailable=false")
    List<UserHabit>findByUserAndNotInChallengeAndUnavailableFalse(@Param("user")User user);
}
