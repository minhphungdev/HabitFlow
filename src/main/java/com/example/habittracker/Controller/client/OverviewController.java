package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.DailyDTO;
import com.example.habittracker.DTO.DiaryDTO;
import com.example.habittracker.DTO.HabitDTO;
import com.example.habittracker.DTO.TodoDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/overview")
public class OverviewController {
    private final JwtUtil jwtUtil;
    private final TokenUtil tokenUtil;
    private final UserService userService;
    private final HabitService habitService;
    private final DailyService dailyService;
    private final ChallengeService challengeService;
    private final TodoService todoService;
    private final DiaryService diaryService;
    private final AchievementService achievementService;

    public OverviewController(JwtUtil jwtUtil, TokenUtil tokenUtil, UserService userService, HabitService habitService, DailyService dailyService, ChallengeService challengeService, TodoService todoService, DiaryService diaryService, AchievementService achievementService) {
        this.jwtUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
        this.userService = userService;
        this.habitService = habitService;
        this.dailyService = dailyService;
        this.challengeService = challengeService;
        this.todoService = todoService;
        this.diaryService = diaryService;
        this.achievementService = achievementService;
    }

    @GetMapping("")
    public String overview(HttpServletRequest request,Model model) {
        // User data
        User user = getUserFromRequest(request);
        model.addAttribute("user", user);

        Achievement achievement = this.achievementService.getAchievementById(user.getAchieveId());
        model.addAttribute("userAchievement", achievement);

        // Challenge data
        List<UserChallenge> userChallenges  = this.challengeService.getChallenges(user.getUserId());
        model.addAttribute("userChallenges", userChallenges);

        List<UserChallenge> userChallengeOwner = this.challengeService.getChallengesOwner(user.getUserId());
        model.addAttribute("userChallengeOwner", userChallengeOwner);

        // Habits
        model.addAttribute("newHabit",new HabitDTO());
        List<UserHabit> userhabit = this.habitService.getUserHabits(user);
        model.addAttribute("userHabits",userhabit);

        // Dailies
        model.addAttribute("newDaily",new DailyDTO());
        List<UserDaily> userdaily = this.dailyService.getUserDaily(user);
        model.addAttribute("userDailies",userdaily);

        //Todos
        model.addAttribute("newTodo", new TodoDTO());
        List<Todo> activeTodos = this.todoService.getActiveTodos(user);
        model.addAttribute("activeTodos", activeTodos);

        //Diary
        model.addAttribute("newDiary", new DiaryDTO());
        List<Diary> diaries = diaryService.getDiariesByUser(user);
        model.addAttribute("diaries", diaries);


        return "client/overview";
    }

    @GetMapping("/detail")
    public String overviewChallengeDetail(HttpServletRequest request, Model model, @RequestParam("challenge")Long challengeId) {
        User user = getUserFromRequest(request);
        model.addAttribute("user", user);

        Achievement achievement = this.achievementService.getAchievementById(user.getAchieveId());
        model.addAttribute("userAchievement", achievement);

        Challenge challenge = this.challengeService.getChallengeById(challengeId);
        //challenge
        UserChallenge userChallenge = this.challengeService.getUserChallenge(user,challenge);
        model.addAttribute("userChallenge", userChallenge);

        // Challenge data
        List<UserChallenge> userChallenges  = this.challengeService.getChallenges(user.getUserId());
        model.addAttribute("userChallenges", userChallenges);

        List<UserChallenge> userChallengeOwner = this.challengeService.getChallengesOwner(user.getUserId());
        model.addAttribute("userChallengeOwner", userChallengeOwner);


        model.addAttribute("newHabit",new HabitDTO());
        List<UserHabit> userhabit = this.habitService.getUserHabitsChallenge(user,challenge);
        model.addAttribute("userHabits",userhabit);

        model.addAttribute("newDaily",new DailyDTO());
        List<UserDaily> userdaily = this.dailyService.getUserDailyChallenge(user,challenge);
        model.addAttribute("userDailies",userdaily);

        model.addAttribute("newTodo", new TodoDTO());
        List<Todo> activeTodos = this.todoService.getActiveTodos(user);
        model.addAttribute("activeTodos", activeTodos);

        model.addAttribute("newDiary", new DiaryDTO());
        List<Diary> diaries = diaryService.getDiariesByUser(user);
        model.addAttribute("diaries", diaries);
        return "client/overview";
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}
