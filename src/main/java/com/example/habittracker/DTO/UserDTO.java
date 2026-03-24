package com.example.habittracker.DTO;

import com.example.habittracker.Domain.User;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String username;
    private String avatar;
    private String email;
    private Long achieveId;
    private String role;
    private MultipartFile image;
    private String oldPassword;
    private String password;
    private String confirmPassword;
    private boolean justLoginWithGoogle;
    private String achievementTitleSelected;

    //thời gian kể từ khi đăng ký
    private Long durationRegister;

    private boolean isLocked;

    private final User.Role[] roleOption = User.Role.values();
}
