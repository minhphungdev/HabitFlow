package com.example.habittracker.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank(message = "Nhập tên người dùng")
    private String userName;
    @NotBlank(message = "Nhập mật khẩu")
    private String password;
    private String avatar;
    @NotBlank(message = "Nhập email")
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Long coins;
    private Long limitCoinsEarnedPerDay = 0L;
    private String token;
    //kiểm tra chưa đăng nhập gửi email
    private LocalDateTime lastLogin;

    private Long challengeLimit;
    private Long taskLimit;
    private Long achieveId;
    private LocalDateTime createAt;
    private boolean isLocked;

    private Integer streakProtectionCount = 0;

    @OneToMany(mappedBy = "user")
    List<UserHabit> userHabits;

    @OneToMany(mappedBy = "user")
    List<UserDaily> userDailies;

    @OneToMany(mappedBy = "user")
    List<Todo> todos;

    @OneToMany(mappedBy = "user")
    List<UserChallenge> userChallenges;

    @OneToMany(mappedBy = "user")
    List<Reward> rewards;


    @OneToMany(mappedBy = "user")
    List<UserAchievement> userAchievements;


    @OneToMany(mappedBy = "user")
    List<Diary> diaries;


    public enum Role{
        ADMIN,USER
    }
}
