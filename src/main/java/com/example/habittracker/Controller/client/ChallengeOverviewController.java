package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.ChallengeDTO;
import com.example.habittracker.Domain.Achievement;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserChallenge;
import com.example.habittracker.Service.AchievementService;
import com.example.habittracker.Service.ChallengeService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/challenge_overview")
public class ChallengeOverviewController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenUtil tokenUtil;
    private final ChallengeService challengeService;
    private final AchievementService achievementService;

    public ChallengeOverviewController(UserService userService, JwtUtil jwtUtil, TokenUtil tokenUtil, ChallengeService challengeService, AchievementService achievementService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
        this.challengeService = challengeService;
        this.achievementService = achievementService;
    }

    @GetMapping()
    public String challenges(HttpServletRequest request, Model model) {

        User user = getUserFromRequest(request);

        model.addAttribute("user", user);

        Achievement achievement = this.achievementService.getAchievementById(user.getAchieveId());
        model.addAttribute("userAchievement", achievement);

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("months", new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});


        List<UserChallenge> userChallenges  = this.challengeService.getValidChallenges(user.getUserId());
        model.addAttribute("challenges", userChallenges);
        model.addAttribute("newChallenge", new ChallengeDTO());
        model.addAttribute("daysOfWeek", List.of("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"));



        return "client/challenge";
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}

