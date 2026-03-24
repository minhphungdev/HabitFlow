package com.example.habittracker.Repository;

import com.example.habittracker.Domain.UserChallenge;
import com.example.habittracker.Domain.UserChallengeDailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeDPRepository extends JpaRepository<UserChallengeDailyProgress, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM UserChallengeDailyProgress dp WHERE dp.userChallenge=:userChallenge")
    void deleteAllByUserChallenge(@Param("userChallenge")UserChallenge userChallenge);

    @Query("SELECT dp FROM UserChallengeDailyProgress dp WHERE dp.userChallenge=:userChallenge AND dp.date=:date")
    Optional<UserChallengeDailyProgress> findByUserChallengeAndDate(@Param("userChallenge") UserChallenge userChallenge, @Param("date") LocalDate date);

    @Query("SELECT dp FROM UserChallengeDailyProgress dp WHERE dp.userChallenge = :userChallenge AND dp.date BETWEEN :start AND :today ORDER BY dp.date DESC")
    List<UserChallengeDailyProgress> findByUserChallengeAndDateBetweenOrderByDateAsc(@Param("userChallenge")UserChallenge userChallenge, @Param("start") LocalDate start, @Param("today") LocalDate today);
}
