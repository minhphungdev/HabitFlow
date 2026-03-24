package com.example.habittracker.Repository;

import com.example.habittracker.Domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitHistoryRepository extends JpaRepository<HabitHistory, Long> {
    @Query("SELECT hs FROM HabitHistory hs WHERE hs.userHabit = :userHabit AND hs.date = :today")
    Optional<HabitHistory> findByUserHabitAndDate(@Param("userHabit")UserHabit userHabit,@Param("today") LocalDate date);

    @Query("SELECT hs FROM HabitHistory hs WHERE hs.userHabit = :userHabit")
    List<HabitHistory> findAllByUserHabit(@Param("userHabit") UserHabit userHabit);

    @Query("SELECT hh.userHabit.userHabitId FROM HabitHistory hh WHERE hh.userHabit.user = :user AND hh.isCompleted = true AND DATE(hh.date) = :date")
    List<Long> findCompletedHabitIdsByUserAndDate(@Param("user") User user, @Param("date")LocalDate date);

    @Query("SELECT hh FROM HabitHistory hh WHERE hh.userHabit.user = :user AND hh.isCompleted = true AND DATE(hh.date) = :date")
    List<HabitHistory> findCompletedHabitHisByUserAndDate(@Param("user") User user, @Param("date")LocalDate date);

    @Query("SELECT COUNT(hh) FROM HabitHistory hh WHERE hh.userHabit IN :userHabits AND hh.date=:date AND hh.isCompleted=true ")
    Long countByUserHabitInAndDateAndIsCompletedTrue(@Param("userHabits")List<UserHabit> userHabits, @Param("date")LocalDate date);

    @Query("SELECT COUNT(hh) FROM HabitHistory hh WHERE hh.userHabit=:userHabit AND hh.date BETWEEN :start AND :end AND hh.isCompleted=true")
    Long countByUserHabitAndDateBetweenAndIsCompletedTrue(@Param("userHabit")UserHabit userHabit, @Param("start")LocalDate startDate, @Param("end")LocalDate endDate);

    @Query("SELECT hh FROM HabitHistory hh WHERE hh.userHabit = :userHabit AND hh.date =:date")
    Optional<HabitHistory> findDailyHistory(@Param("userHabit")UserHabit userHabit, @Param("date")LocalDate date);

    @Query("SELECT hh.coinEarned FROM HabitHistory hh WHERE hh.userHabit = :userHabit AND hh.date = :today")
    Long findCoinEarnByUserHabitAndDate(@Param("userHabit")UserHabit userHabit, @Param("today")LocalDate date);

    @Query("SELECT COUNT(*) FROM HabitHistory hh WHERE hh.userHabit = :userHabit AND hh.isCompleted = true")
    Long countCompleteHabit(@Param("userHabit") UserHabit userHabit);
}
