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
public interface DailyHistoryRepository extends JpaRepository<DailyHistory, Long> {
    @Query("SELECT ds FROM DailyHistory ds WHERE ds.userDaily=:userDaily AND ds.date=:date")
    Optional<DailyHistory> findDailyHistory(@Param("userDaily") UserDaily userDaily, @Param("date") LocalDate date);

    @Query("SELECT dh FROM DailyHistory dh WHERE dh.userDaily = :userDaily ORDER BY dh.date DESC LIMIT 1")
    Optional<DailyHistory> findTopByUserDailyOrderByDateDesc(@Param("userDaily") UserDaily userDaily);

    @Query("SELECT dh FROM DailyHistory dh WHERE dh.userDaily = :userDaily ORDER BY dh.date DESC ")
    List<DailyHistory> findAllByUserDailyOrderByDateDesc(@Param("userDaily") UserDaily userDaily);

    @Query("SELECT dh FROM DailyHistory dh WHERE dh.userDaily = :userDaily")
    List<DailyHistory> findAllByUserDaily(@Param("userDaily") UserDaily userDaily);

    @Query("SELECT dh.userDaily.userDailyId FROM DailyHistory dh WHERE dh.userDaily.user = :user AND dh.isCompleted = true AND DATE(dh.date) = :date")
    List<Long> findCompletedDailyIdsByUserAndDate(@Param("user")User user, @Param("date")LocalDate date);

    @Query("SELECT COUNT(dh) FROM DailyHistory dh WHERE dh.userDaily IN :userDailies AND dh.date=:date AND dh.isCompleted=true ")
    Long countByUserDailyInAndDateAndIsCompletedTrue(@Param("userDailies")List<UserDaily> userDailies, @Param("date")LocalDate date);

    @Query("SELECT COUNT(dh) FROM DailyHistory dh WHERE dh.userDaily=:userDaily AND dh.date BETWEEN :start AND :end AND dh.isCompleted=true")
    Long countByUserDailyAndDateBetweenAndIsCompletedTrue(@Param("userDaily")UserDaily userDaily, @Param("start")LocalDate startDate, @Param("end")LocalDate endDate);

    @Query("SELECT dh.coinEarned FROM DailyHistory dh WHERE dh.userDaily = :userDaily AND dh.date = :today")
    Long findCoinEarnedByUserDailyAndUser(@Param("userDaily")UserDaily userDaily,@Param("today")LocalDate today);

    @Query("SELECT COUNT(*) FROM DailyHistory dh WHERE dh.userDaily = :userDaily AND dh.isCompleted = true")
    Long countCompleteDaily(@Param("userDaily") UserDaily userDaily);
}
