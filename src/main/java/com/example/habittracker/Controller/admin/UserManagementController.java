package com.example.habittracker.Controller.admin;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Service.AuthService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthService authService;

    public UserManagementController(TokenUtil tokenUtil, JwtUtil jwtUtil, UserService userService, AuthService authService) {
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("")
    public String userManagement(HttpServletRequest request, Model model,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "7") int size,
                                 @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        User userAdmin = getUserAdmin(request);
        model.addAttribute("userAdmin", userAdmin);

        List<UserDTO> newUsers = this.userService.getAllNewUser();
        model.addAttribute("newUsers", newUsers);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = this.userService.getAllUsersBySearch(search,pageable);
        model.addAttribute("users", users);
        model.addAttribute("search", search);

        UserDTO createUser = new UserDTO();
        model.addAttribute("createUpdateUser", createUser);

        return "admin/usermanage";
    }

    @PostMapping("/create")
    public String createNewUser(@ModelAttribute("createUpdateUser")UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try{
            this.authService.AdminCreateUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo người dùng thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Tạo người dùng thất bại!"+e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }


    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUpdateUser(@PathVariable Long userId, Model model) {
        UserDTO userUpdate = this.userService.getUserUpdate(userId);

        return ResponseEntity.ok().body(userUpdate);
    }

    @PostMapping("")
    public String updateUser(@ModelAttribute("userUpdate")UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try{
            this.userService.AdminUpdateUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Cập nhật người dùng thất bại!"+e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/lock/{userId}")
    public String lockUser(@PathVariable("userId") Long userId, RedirectAttributes redirectAttributes) {
        try{
            boolean lockOrUnlock = this.userService.LockUser(userId);
            if (lockOrUnlock) {
                redirectAttributes.addFlashAttribute("success","Khóa người dùng thành công!");
            }else {
                redirectAttributes.addFlashAttribute("success","Mở khóa người dùng thành công!");
            }
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Khóa/Mở khóa người dùng thất bại!"+e.getMessage());
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users";
    }

    public User getUserAdmin(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email = jwtUtil.getEmailFromToken(token);

        return this.userService.getUser(email);
    }
}
