package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.AchievementDTO;
import com.example.habittracker.DTO.CalendarDTO;
import com.example.habittracker.DTO.ChallengeDTO;
import com.example.habittracker.DTO.LoginResponse;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.UserAchievementRepository;
import com.example.habittracker.Repository.UserChallengeRepository;
import com.example.habittracker.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Controller
@RequestMapping("")
public class HomeController {
    private final UserService userService;
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final ChallengeService challengeService;
    private final CalendarService calendarService;
    private final UserChallengeRepository userChallengeRepository;
    private final AchievementService achievementService;
    private final UserAchievementRepository userAchievementRepository;
    private final SystemRewardProvider systemRewardProvider;


    public HomeController(UserService userService, TokenUtil tokenUtil, JwtUtil jwtUtil, ChallengeService challengeService, CalendarService calendarService, UserChallengeRepository userChallengeRepository, AchievementService achievementService, UserAchievementRepository userAchievementRepository, SystemRewardProvider systemRewardProvider) {
        this.userService = userService;
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.challengeService = challengeService;
        this.calendarService = calendarService;
        this.userChallengeRepository = userChallengeRepository;
        this.achievementService = achievementService;
        this.userAchievementRepository = userAchievementRepository;
        this.systemRewardProvider = systemRewardProvider;
    }

    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        User user = getUserFromRequest(request);

        model.addAttribute("user", user);

        Achievement achievement = this.achievementService.getAchievementById(user.getAchieveId());
        model.addAttribute("userAchievement", achievement);

        //challenge
        List<UserChallenge> userChallenges  = this.challengeService.getAllActiveChallenges(user);
        model.addAttribute("userChallenges", userChallenges);

        //welcome box challenge
        UserChallenge userChallenge = null;
        if(!userChallenges.isEmpty()) {
            int randomUserChallenge = ThreadLocalRandom.current().nextInt(userChallenges.size());
            userChallenge = userChallenges.get(randomUserChallenge);
        }

        model.addAttribute("challengeWelcomeBox", userChallenge);

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        model.addAttribute("user", user);

        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("today", today);
        model.addAttribute("months", new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});

        model.addAttribute("newReward", new Reward());
        model.addAttribute("updateReward", new Reward());

        //system Reward
        model.addAttribute("systemRewards", systemRewardProvider.getSystemRewards());
        return "client/home";
    }

    @GetMapping("/calendar/{date}")
    @ResponseBody
    public ResponseEntity<CalendarDTO> getCalendarData(@PathVariable String date, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        CalendarDTO calenderResponse = this.calendarService.calendarResponse(user, date);

        return ResponseEntity.ok(calenderResponse);
    }



    @GetMapping("/check-on-login")
    @ResponseBody
    public ResponseEntity<LoginResponse> checkChallengesOnLogin(HttpServletRequest request) {
        User user = getUserFromRequest(request);

        // Lấy danh sách thử thách hoàn thành cần thông báo
        List<UserChallenge> completedChallenges = challengeService.getCompletedChallengesForNotification(user);

        //Lấy thành tựu đạt được để thông báo
        List<UserAchievement> userAchievements = this.achievementService.getAchievementReceive(user);

        LoginResponse response = new LoginResponse();
        response.setChallengesCompleted(completedChallenges.stream()
                .map(userChallenge -> ChallengeDTO.builder()
                        .challengeId(userChallenge.getChallenge().getChallengeId())
                        .title(userChallenge.getChallenge().getTitle())
                        .progress(userChallenge.getProgress())
                        .status(userChallenge.getStatus())
                        .coinEarn(userChallenge.getCoinEarn())
                        .build()
                )
                .collect(Collectors.toList()));

        response.setAchievementsCompleted(userAchievements.stream()
                .map(userAchievement -> AchievementDTO.builder()
                        .achievementTitle(userAchievement.getAchievement().getTitle())
                        .achievementDescription(userAchievement.getAchievement().getDescription())
                        .bonusTask(userAchievement.getAchievement().getTaskBonus())
                        .bonusChallenge(userAchievement.getAchievement().getChallengeBonus())
                        .build()).collect(Collectors.toList()));

        completedChallenges.forEach(userChallenge -> {
            userChallenge.setNotificationShown(true);
            userChallengeRepository.save(userChallenge);
        });

        userAchievements.forEach(userAchievement -> {
            userAchievement.setNotification(true);
            userAchievementRepository.save(userAchievement);
        });

        return ResponseEntity.ok().body(response);
    }
    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}
