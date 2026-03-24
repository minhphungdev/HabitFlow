package com.example.habittracker.Repository;

import com.example.habittracker.Domain.PasswordResetToken;
import com.example.habittracker.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.user=:user")
    void deleteByUser(@Param("user")User user);
}
