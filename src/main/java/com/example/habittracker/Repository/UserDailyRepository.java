package com.example.habittracker.Repository;

import com.example.habittracker.Domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyRepository extends JpaRepository<UserDaily,Long> {
    Optional<UserDaily> findByUserAndDaily(User user, Daily daily);

    @Query("SELECT ud FROM UserDaily ud WHERE ud.user =:user AND ud.daily.challenge = :challenge AND ud.unavailable = false ")
    List<UserDaily> findByUserAndDailyChallengeAndUnavailableFalse(@Param("user")User user,@Param("challenge") Challenge challenge);

    @Query("SELECT ud FROM UserDaily ud WHERE ud.user.userId = :userId")
    List<UserDaily> findByUserId(@Param("userId")Long userId);

    @Query("SELECT ud FROM UserDaily ud WHERE ud.user =:user AND ud.daily.challenge =null AND ud.unavailable=false")
    List<UserDaily>findByUserAndNotInChallengeAndUnavailableFalse(@Param("user")User user);
}
