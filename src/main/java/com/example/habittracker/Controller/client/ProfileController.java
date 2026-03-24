package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.MessageResponse;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final ChallengeService challengeService;
    private final DiaryService diaryService;
    private final AchievementService achievementService;
    private final HabitService habitService;
    private final DailyService dailyService;
    private final TodoService todoService;

    public ProfileController(UserService userService, TokenUtil tokenUtil, JwtUtil jwtUtil, ChallengeService challengeService, DiaryService diaryService, AchievementService achievementService, HabitService habitService, DailyService dailyService, TodoService todoService) {
        this.userService = userService;
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.challengeService = challengeService;
        this.diaryService = diaryService;
        this.achievementService = achievementService;
        this.habitService = habitService;
        this.dailyService = dailyService;
        this.todoService = todoService;
    }

    @GetMapping("")
    public String profile(HttpServletRequest request,Model model) {
        User user = getUserFromRequest(request);
        model.addAttribute("user", user);

        Achievement achievement = this.achievementService.getAchievementById(user.getAchieveId());
        model.addAttribute("userAchievement", achievement);

        model.addAttribute("newUser", user);
        UserDTO userDTO = this.userService.UserChangePassword(user);
        model.addAttribute("changePassword", userDTO);
        List<UserAchievement> userAchievements = this.achievementService.getUserAchievementReceive(user);
        model.addAttribute("achievements", userAchievements);

        List<UserChallenge> getParticipateChallenge = this.challengeService.getChallenges(user.getUserId());
        model.addAttribute("participatingChallenges",getParticipateChallenge);

        UserChallenge longestStreakChallenge = this.challengeService.getLongestStreakUserChallenges(user.getUserId());
        model.addAttribute("longestStreakChallenge",longestStreakChallenge);

        List<Diary> journalEntries = this.diaryService.getDiariesByUser(user);
        model.addAttribute("journalEntries",journalEntries);

        List<UserChallenge>completedChallenges = this.challengeService.getAllCompleteChallenge(user);
        model.addAttribute("completedChallenges",completedChallenges);

        Integer rankUser = this.userService.getUserRank(user.getUserId());
        model.addAttribute("rankUser",rankUser);

        long completedTask = this.userService.getTaskComplete(user,true);
        model.addAttribute("completedTask",completedTask);

        long habitsCount = this.habitService.countCompleteHabit(user);
        model.addAttribute("habitsCount",habitsCount);

        long dailiesCount = this.dailyService.countCompleteDaily(user);
        model.addAttribute("dailiesCount",dailiesCount);

        long todosCount = this.todoService.countCompleteTodo(user);
        model.addAttribute("todosCount",todosCount);

        return "client/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("newUser") User user, @RequestParam("image") MultipartFile image, RedirectAttributes redirectAttributes) {
        try{
            this.userService.updateUser(user,image);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("fail", "Cập nhật thất bại"+e.getMessage());
            return "redirect:/profile";
        }
        return "redirect:/profile";
    }

    @PostMapping("/change_password")
    public ResponseEntity<MessageResponse> changePassword(@ModelAttribute UserDTO userDTO) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            this.userService.changePassword(userDTO);
            messageResponse.setMessage("Thay đổi mật khẩu thành công!");
            messageResponse.setType("success");
        }catch(RuntimeException e){
            messageResponse.setMessage(e.getMessage());
            messageResponse.setType("fail");
        }
        return ResponseEntity.ok().body(messageResponse);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}
