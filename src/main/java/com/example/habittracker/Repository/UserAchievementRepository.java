package com.example.habittracker.Repository;

import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user AND ua.isNotification=false")
    List<UserAchievement> getUserAchievementReceiveTodayAndNotificationFalse(@Param("date")LocalDate date,@Param("user") User user);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user")
    List<UserAchievement> getUserAchievementReceive(User user);

    @Query("SELECT ua FROM UserAchievement ua WHERE DATE(ua.earnedDate) = :date")
    List<UserAchievement> findAllAllUserAchievementReceiveToday(@Param("date")LocalDate date);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.achievement.achievementId = :achievementId")
    Optional<List<UserAchievement>> findAllByAchievementId(@Param("achievementId")Long achievementId);
}
