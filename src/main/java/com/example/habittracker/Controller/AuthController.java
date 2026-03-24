package com.example.habittracker.Controller;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.DTO.Login;
import com.example.habittracker.DTO.Register;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Repository.UserRepository;
import com.example.habittracker.Service.AuthService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    public AuthController(AuthService authService, UserService userService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userLogin", new Login());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("userLogin") Login login,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            User user = authService.login(login);

            // Lưu token vào cookie
            Cookie cookie = new Cookie("token", user.getToken());
            cookie.setHttpOnly(true);
            cookie.setMaxAge(5 * 60 * 60); // 5 giờ
            cookie.setPath("/");
            response.addCookie(cookie);

            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/home";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new Register());
        return "auth/register";
    }
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") Register register,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(register);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login_with_google")
    public String loginWithGoogle(HttpServletResponse response ,@AuthenticationPrincipal OAuth2User principal, RedirectAttributes redirectAttributes) {
        try{
            String username = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            String avatar = principal.getAttribute("picture");

            User user = this.userService.findUserByEmail(email);
            if(user == null) {
                user = this.authService.createOAuth2User(username,email,avatar);
            }
            if(user.isLocked()) {
                throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
            user.setToken(token);
            user.setLastLogin(LocalDateTime.now());
            this.userRepository.save(user);

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(5 * 60 * 60); // 5 giờ
            cookie.setPath("/");
            response.addCookie(cookie);

            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/home";
            }
        }catch(RuntimeException e) {
            redirectAttributes.addFlashAttribute("fail", "Lỗi xảy ra khi đăng nhập! " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPasswordRequest(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            this.authService.createPasswordResetTokenAndSendEmail(email);
            redirectAttributes.addFlashAttribute("success", " Một email chứa liên kết đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        UserDTO userDTO = this.authService.validatePasswordResetToken(token);
        model.addAttribute("userDTO", userDTO);
        return "auth/reset_password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@ModelAttribute("userDTO")UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try {
            this.authService.resetPassword(userDTO);
            redirectAttributes.addFlashAttribute("success", "Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
            return "redirect:/login";
        }
        return "redirect:/login";
    }
}
