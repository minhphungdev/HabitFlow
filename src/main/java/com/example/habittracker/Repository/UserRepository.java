package com.example.habittracker.Repository;

import com.example.habittracker.DTO.UserChallengeStats;
import com.example.habittracker.Domain.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsUserByEmail(String email);

    boolean existsUserByUserName(String userName);

    Optional<User> findUserByUserName(String userName);

    Optional<User> findByEmail(@NotBlank(message = "Nhập email") String email);

    @Query("SELECT u AS user,COUNT(uc) AS completedChallengesCount FROM User u LEFT JOIN u.userChallenges uc WHERE uc.status = 'COMPLETE' GROUP BY u ORDER BY COUNT(uc) DESC")
    List<UserChallengeStats> findAllUsersOrderedByCompletedChallenges();

    @Query("SELECT u FROM User u WHERE FUNCTION('DATE', u.createAt) = :date AND u.role = 'USER' ORDER BY u.createAt DESC " )
    List<User> findAllUserRegisterToday(@Param("date")LocalDate date);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String search, String search1, Pageable pageable);
}
