package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Achievement;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    @Query("SELECT a FROM Achievement a WHERE a.achievementId=:achievementId")
    Achievement getAchievementById(@Param("achievementId")Long achievementId);

    Page<Achievement> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String search, String search1, Pageable pageable);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user=:user AND ua.achievement=:achievement")
    Optional<UserAchievement> getUserAchievemenByUserAchievement(@Param("user") User user, @Param("achievement")Achievement achievement);
}
