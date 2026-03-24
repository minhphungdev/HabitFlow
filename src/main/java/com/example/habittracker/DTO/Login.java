package com.example.habittracker.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Login {
    private String userName;
    @NotBlank(message = "Yêu cầu Email")
    private String email;
    @NotBlank(message = "Yêu cầu mật khẩu")
    private String password;
}
