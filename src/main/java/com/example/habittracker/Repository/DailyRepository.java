package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Daily;
import com.example.habittracker.Domain.UserDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRepository extends JpaRepository<Daily,Long> {
    @Query("SELECT ud.daily FROM UserDaily ud WHERE ud.user.userId = :userId")
    Optional<List<Daily>> findDailiesForUser(@Param("userId") Long userId);

    @Query("SELECT ud FROM UserDaily ud WHERE ud.user.userId = :userId ORDER BY ud.userDailyId DESC")
    List<UserDaily> findUserDailiesByUserId(@Param("userId") Long userId);

@Query("SELECT ud FROM UserDaily ud WHERE ud.daily.challenge.challengeId = :challengeId")
    List<UserDaily> findByChallenge_ChallengeId(@Param("challengeId")Long challengeId);

    @Query("SELECT d FROM Daily d WHERE d.challenge.challengeId = :challengeId")
    List<Daily> findByChallengeId(@Param("challengeId")Long challengeId);
}
