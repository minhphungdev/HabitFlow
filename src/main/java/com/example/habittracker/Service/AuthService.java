package com.example.habittracker.Service;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.DTO.Login;
import com.example.habittracker.DTO.Register;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.Achievement;
import com.example.habittracker.Domain.PasswordResetToken;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserAchievement;
import com.example.habittracker.Repository.PasswordResetTokenRepository;
import com.example.habittracker.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ImageService imageService;
    private final AchievementService achievementService;
    private String folder = "user_avatar";
    public static String defaultPassAuth = "oauth2_user_password";
    private final Long coinsDefault = 0L;
    private final Long challengeLimitDefault = 3L;
    private final Long taskLimitDefault = 10L;
    private final Long limitCoinsEarnedPerDayDefault = 0L;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final int EXPIRY_DATE = 30;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, ImageService imageService, AchievementService achievementService, EmailService emailService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.imageService = imageService;
        this.achievementService = achievementService;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public void register(Register register) {
        if (userRepository.existsUserByEmail(register.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (!register.getPassword().equals(register.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }

        Achievement achievement = this.achievementService.getAchievementById(1L);

        User user = User.builder()
                .userName(register.getUsername())
                .password(passwordEncoder.encode(register.getPassword()))
                .email(register.getEmail())
                .role(User.Role.USER)
                .coins(coinsDefault)
                .challengeLimit(challengeLimitDefault)
                .taskLimit(taskLimitDefault)
                .limitCoinsEarnedPerDay(limitCoinsEarnedPerDayDefault)
                .createAt(LocalDateTime.now())
                .achieveId(achievement.getAchievementId())
                .isLocked(false)
                .streakProtectionCount(0)
                .build();
        userRepository.save(user);
        UserAchievement userAchievement = UserAchievement.builder().user(user).achievement(achievement).earnedDate(LocalDateTime.now()).isNotification(false).build();

        this.achievementService.saveUserAchievement(userAchievement);

        this.emailService.sendWelcomeEmail(user);
    }

    public User login(Login login) {
        User user = userRepository.findByEmail(login.getEmail()).orElseThrow(()->new RuntimeException("Không tìm thấy Email!"));
        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }
        if(user.isLocked()){
            throw new RuntimeException("Tài khoản của bạn hiện đã bị khóa!");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
        user.setToken(token);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User createOAuth2User(String username, String email, String avatar){
        Achievement achievement = this.achievementService.getAchievementById(1L);

        User user = User.builder()
                .userName(username)
                .email(email)
                .password(passwordEncoder.encode(defaultPassAuth))
                .role(User.Role.USER)
                .coins(coinsDefault)
                .challengeLimit(challengeLimitDefault)
                .taskLimit(taskLimitDefault)
                .limitCoinsEarnedPerDay(limitCoinsEarnedPerDayDefault)
                .createAt(LocalDateTime.now())
                .achieveId(achievement.getAchievementId())
                .isLocked(false)
                .streakProtectionCount(0)
                .build();

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String filePath = imageService.saveImageFromUrl(avatar, folder);
                user.setAvatar(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
            }
        }
        this.userRepository.save(user);

        UserAchievement userAchievement = UserAchievement.builder().user(user).achievement(achievement).earnedDate(LocalDateTime.now()).isNotification(false).build();
        this.achievementService.saveUserAchievement(userAchievement);

        this.emailService.sendWelcomeEmail(user);
        return user;
    }

    @Transactional
    public void AdminCreateUser(UserDTO userDTO) {
        if (userRepository.existsUserByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }

        Achievement achievement = this.achievementService.getAchievementById(1L);


        User user = User.builder()
                .userName(userDTO.getUsername())
                .email(userDTO.getEmail())
                .createAt(LocalDateTime.now())
                .coins(coinsDefault)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole().equals("ADMIN")?User.Role.ADMIN:User.Role.USER)
                .limitCoinsEarnedPerDay(limitCoinsEarnedPerDayDefault)
                .challengeLimit(challengeLimitDefault)
                .taskLimit(taskLimitDefault)
                .achieveId(achievement.getAchievementId())
                .isLocked(false)
                .streakProtectionCount(0)
                .build();
        this.userRepository.save(user);
        UserAchievement userAchievement = UserAchievement.builder().user(user).achievement(achievement).earnedDate(LocalDateTime.now()).isNotification(false).build();
        this.achievementService.saveUserAchievement(userAchievement);
        this.emailService.sendWelcomeEmail(user);
    }

    @Transactional
    public void createPasswordResetTokenAndSendEmail(String email){
        User user = this.userRepository.findByEmail(email).orElse(null);
        if(user != null){
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(EXPIRY_DATE))
                    .build();
            this.passwordResetTokenRepository.save(passwordResetToken);

            this.emailService.sendPasswordResetEmail(user,token);
        }
    }

    @Transactional
    public UserDTO validatePasswordResetToken(String token){
        PasswordResetToken resetToken = this.passwordResetTokenRepository.findByToken(token);
        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            this.passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Đã hết hạn phiên đặt lại mật khẩu.Vui lòng thử lại!");
        }
        return UserDTO.builder()
                .userId(resetToken.getUser().getUserId())
                .build();
    }

    @Transactional
    public void resetPassword(UserDTO userDTO){
        User user = this.userRepository.findById(userDTO.getUserId()).orElse(null);
        if(user != null){
            if(userDTO.getPassword().equals(userDTO.getConfirmPassword())){
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                this.userRepository.save(user);
            }
        }
        this.passwordResetTokenRepository.deleteByUser(user);
    }
}
