package com.example.habittracker.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Register {
    @NotBlank(message = "yêu cầu tên người dùng")
    private String username;
    @NotBlank(message = "yêu cầu mật khẩu")
    private String password;
    @NotBlank(message = "yêu cầu email")
    @Email(message = "Sai định dạng email")
    private String email;
    @NotBlank(message = "yêu cầu xác nhận mật khẩu")
    private String confirmPassword;
}
