package com.example.habittracker.Controller.admin;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.AchievementDTO;
import com.example.habittracker.Domain.Achievement;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Service.AchievementService;
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

import java.util.List;

@Controller
@RequestMapping("/admin/achievements")
public class AchievementController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenUtil tokenUtil;
    private final AchievementService achievementService;

    public AchievementController(UserService userService, JwtUtil jwtUtil, TokenUtil tokenUtil, AchievementService achievementService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
        this.achievementService = achievementService;
    }

    @GetMapping("")
    public String achievements(HttpServletRequest request, Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "7") int size,
                               @RequestParam(value = "search", required = false, defaultValue = "") String search) {

        User userAdmin = getUserAdmin(request);
        model.addAttribute("userAdmin", userAdmin);

        Pageable pageable = PageRequest.of(page, size);
        Page<Achievement> achievements = this.achievementService.getAchievementsBySearch(search,pageable);
        model.addAttribute("achievements", achievements);
        model.addAttribute("search", search);

        List<AchievementDTO> userAchievements = this.achievementService.getAllUserAchievementToday();
        model.addAttribute("userAchievementsToday", userAchievements);

        AchievementDTO achievementDTO = new AchievementDTO();
        model.addAttribute("achievement", achievementDTO);

        return "admin/achievementsmanage";
    }

    @PostMapping("/add")
    public String addAchievement(@ModelAttribute("achievement")AchievementDTO achievementDTO, RedirectAttributes redirectAttributes) {

        try{
            this.achievementService.addNewAchievement(achievementDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo thành tựu thành công!");
        }catch(RuntimeException e){
           redirectAttributes.addFlashAttribute("fail","Tạo thành tựu thất bại!"+e.getMessage());
           return "redirect:/admin/achievements";
        }

        return "redirect:/admin/achievements";
    }

    @GetMapping("/{achievementId}")
    @ResponseBody
    public ResponseEntity<AchievementDTO> getUpdateAchievement(@PathVariable("achievementId")Long achievementId) {
        AchievementDTO achievementDTO = this.achievementService.getUpdateAchievementById(achievementId);
        return ResponseEntity.ok().body(achievementDTO);
    }

    @PostMapping("/update")
    public String updateAchievement(@ModelAttribute("achievement")AchievementDTO achievementDTO, RedirectAttributes redirectAttributes) {

        try{
            this.achievementService.updateAchievement(achievementDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành tựu thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail","Cập nhật thành tựu thất bại!"+e.getMessage());
            return "redirect:/admin/achievements";
        }

        return "redirect:/admin/achievements";
    }

    @GetMapping("/{achievementId}/hide")
    public String hideAchievement(@PathVariable("achievementId")Long achievementId, RedirectAttributes redirectAttributes) {
        try{
            this.achievementService.hideAchievement(achievementId);
            redirectAttributes.addFlashAttribute("success", "Ẩn/Hiện thành tựu thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Ẩn/Hiện thành tựu thất bại!");
            return "redirect:/admin/achievements";
        }
        return "redirect:/admin/achievements";
    }

    @GetMapping("/delete/{achievementId}")
    public String deleteAchievement(@PathVariable("achievementId")Long achievementId, RedirectAttributes redirectAttributes) {
        try{
            this.achievementService.deleteAchievement(achievementId);
            redirectAttributes.addFlashAttribute("success", "Xóa thành tựu thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Xóa thành tựu thành công!");
            return "redirect:/admin/achievements";
        }
        return "redirect:/admin/achievements";
    }

    public User getUserAdmin(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email = jwtUtil.getEmailFromToken(token);

        return this.userService.getUser(email);
    }
}
