package com.example.habittracker.Config;

import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserDaily;
import com.example.habittracker.Domain.UserHabit;
import com.example.habittracker.Repository.UserRepository;
import com.example.habittracker.Service.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
public class SchedulerConfig {
    private final HabitService habitService;
    private final DailyService dailyService;
    private final ChallengeService challengeService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final AchievementService achievementService;

    public SchedulerConfig(HabitService habitService, DailyService dailyService, ChallengeService challengeService, UserRepository userRepository, EmailService emailService, UserService userService, AchievementService achievementService) {
        this.habitService = habitService;
        this.dailyService = dailyService;
        this.challengeService = challengeService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.achievementService = achievementService;
    }


//@Scheduled(cron = "*/5 * * * * *")
    @Scheduled(cron ="58 59 23 * * *")
    public void ResetHabitCountAndDailyAndLimitCoinsEarned() {
        this.dailyService.resetDaily();
        this.habitService.resetHabit();
        this.userService.resetLimitCoin();
        System.out.println("method ResetHabitCountAndDailyAndLimitCoinsEarned run ");
    }

   @Scheduled(cron="50 59 23 * * *")
    public void calChallengeProgress(){
        this.challengeService.CalChallengeProgressEndDay();
       System.out.println("method calChallengeProgress run ");
   }

   @Scheduled(cron="30 59 23 * * *")
   public void achievementCheck(){
       List<User> users = userRepository.findAll();
       for (User user : users) {
           this.achievementService.receiveAchievement(user);
       }
       System.out.println("method achievementCheck run ");
    }

   @Scheduled(cron="1 0 0 * * *")
    public void setTaskHistoryAndChallengeProgress(){
        this.habitService.setHabitHistoryNewDay();
        this.dailyService.setDailyHistoryNewDay();
        this.challengeService.calChallengeProgressNewDay();
       System.out.println("method setTaskHistory run ");
   }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkAllChallenges() {
        this.challengeService.checkChallengeCompleted();
        System.out.println("method checkAllChallenges run ");
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void sendUncompletedTasksEmailForAllUsers() {
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            List<UserHabit> userHabitsUnComplete = this.habitService.getHabitIsUnComplete(user);
            List<UserDaily> userDailiesUnComplete = this.dailyService.getDailyUnCompleteEnableToday(user);
            this.emailService.sendEmailTaskUnComplete(userDailiesUnComplete,userHabitsUnComplete, user);
        });
        System.out.println("method sendUncompletedTasksEmailForAllUsers run ");
    }

    @Scheduled(cron="0 0 9 * * *")
    public void sendEmailReminderLogin(){
        List<User> users = userRepository.findAll();
        users.forEach(this.userService::checkUserLogin);
        System.out.println("method sendEmailReminderLogin run ");
    }
}
