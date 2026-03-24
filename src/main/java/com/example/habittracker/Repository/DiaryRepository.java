package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Challenge;
import com.example.habittracker.Domain.Diary;
import com.example.habittracker.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("SELECT d FROM Diary d WHERE d.user = :user ORDER BY d.date DESC")
    List<Diary> findByUser(@Param("user")User user);

    @Query("SELECT ud.diaryId FROM Diary ud WHERE ud.user = :user AND ud.date = :date")
    List<Long> findIdByUserAndDate(@Param("user")User user, @Param("date") LocalDate date);

    @Query("SELECT d FROM Diary d WHERE d.user = :user AND d.challengeId=:challengeId AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC ")
    List<Diary> findDiaryInChallenge(@Param("user")User user, @Param("challengeId") Long challengeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
