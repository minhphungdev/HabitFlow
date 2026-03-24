package com.example.habittracker.Service;

import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeProgressService {
    private final UserDailyRepository userDailyRepository;
    private final UserHabitRepository userHabitRepository;
    private final HabitHistoryRepository habitHistoryRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final UserChallengeDPRepository userChallengeDPRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final CoinCalculationService coinCalculationService;
    private final TodoRepository todoRepository;
    private final DiaryRepository diaryRepository;

    public ChallengeProgressService(UserDailyRepository userDailyRepository, UserHabitRepository userHabitRepository, HabitHistoryRepository habitHistoryRepository, DailyHistoryRepository dailyHistoryRepository, UserChallengeDPRepository userChallengeDPRepository, UserChallengeRepository userChallengeRepository, EmailService emailService, UserService userService, CoinCalculationService coinCalculationService, TodoRepository todoRepository, DiaryRepository diaryRepository) {
        this.userDailyRepository = userDailyRepository;
        this.userHabitRepository = userHabitRepository;
        this.habitHistoryRepository = habitHistoryRepository;
        this.dailyHistoryRepository = dailyHistoryRepository;
        this.userChallengeDPRepository = userChallengeDPRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.coinCalculationService = coinCalculationService;
        this.todoRepository = todoRepository;
        this.diaryRepository = diaryRepository;
    }

    @Transactional
    public void calculateAndSaveDailyProgress(Long userChallengeId, LocalDate targetDate) {
        UserChallenge userChallenge = userChallengeRepository.findById(userChallengeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu(userChallenge)"));

        // lấy daily trong challenge đang hoạt động
        List<UserDaily> activeUserDailies = userDailyRepository.findByUserAndDailyChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());

        // lấy habit trong challenge đang hoạt động
        List<UserHabit> activeUserHabits = userHabitRepository.findByUserAndHabitChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());

        // tính tổng task dự kiến cần hoàn thành trong ngày
        long expectedTasksForDay = 0L;
        for (UserDaily ud : activeUserDailies) {
            if (enableToday(ud, targetDate)) {
                expectedTasksForDay++;
            }
        }
        for (UserHabit uh : activeUserHabits) {
            expectedTasksForDay++;
        }


        // tính tổng task hoàn thành trong ngày
        long completedTasksForDay = 0L;

        completedTasksForDay += dailyHistoryRepository.countByUserDailyInAndDateAndIsCompletedTrue(activeUserDailies, targetDate);

        completedTasksForDay += habitHistoryRepository.countByUserHabitInAndDateAndIsCompletedTrue(activeUserHabits, targetDate);


        // tính phần trăm hoàn thành trong ngày
        int completionPercentage = 0;
        if (expectedTasksForDay > 0) {
            completionPercentage = (int) Math.round((double) completedTasksForDay / expectedTasksForDay * 100.0);
        } else{
            completionPercentage = 100;
        }

        //lưu, cập nhât UserChallengeDailyProgress
        UserChallengeDailyProgress dailyProgress = userChallengeDPRepository.findByUserChallengeAndDate(userChallenge, targetDate)
                .orElse(UserChallengeDailyProgress.builder()
                        .userChallenge(userChallenge)
                        .date(targetDate)
                        .build());

        dailyProgress.setCompletionPercentage(completionPercentage);
        userChallengeDPRepository.save(dailyProgress);

        //lưu ngày hoàn thành cho calendar
        int completionThreshold = 100;
        UserChallengeDailyProgress userChallengeDailyProgress = this.userChallengeDPRepository.findByUserChallengeAndDate(userChallenge,targetDate).get();
        if(userChallengeDailyProgress.getCompletionPercentage() >= completionThreshold){
            if(!userChallenge.getCompletedTasksList().contains(targetDate)){
                userChallenge.getCompletedTasksList().add(targetDate);
                this.userChallengeRepository.save(userChallenge);
            }
        }else {
            if(userChallenge.getCompletedTasksList().contains(targetDate)){
                userChallenge.getCompletedTasksList().remove(targetDate);
                this.userChallengeRepository.save(userChallenge);
            }
        }
    }

    @Transactional
    public void recalculateUserChallengeProgress(UserChallenge userChallenge) {;

        LocalDate challengeStartDate = userChallenge.getStartDate();
        LocalDate challengeEndDate = userChallenge.getEndDate();

        List<UserDaily> activeUserDailies = userDailyRepository.findByUserAndDailyChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());
        List<UserHabit> activeUserHabits = userHabitRepository.findByUserAndHabitChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());

        long totalExpectedTasks = 0L;
        long completedTasksForChart = 0L;
        long skippedTasksForChart = 0L;

        for (UserDaily ud : activeUserDailies) {
            long expectedDailyTasks = calculateExpectedDailyTasksInPeriod(ud, challengeStartDate, challengeEndDate);
            totalExpectedTasks += expectedDailyTasks;

            // Đếm số lần hoàn thành thực tế cho Daily History trong khoảng thời gian thử thách
            long actualCompletedDailyTasks = dailyHistoryRepository.countByUserDailyAndDateBetweenAndIsCompletedTrue(ud, challengeStartDate, challengeEndDate);
            completedTasksForChart += actualCompletedDailyTasks;
        }

        for (UserHabit uh : activeUserHabits) {
            long expectedHabitTasks = calculateExpectedHabitTasksInPeriod(uh, challengeStartDate, challengeEndDate);
            totalExpectedTasks += expectedHabitTasks;

            // Đếm số lần hoàn thành thực tế cho Habit History trong khoảng thời gian thử thách
            long actualCompletedHabitTasks = habitHistoryRepository.countByUserHabitAndDateBetweenAndIsCompletedTrue(uh, challengeStartDate, challengeEndDate);
            completedTasksForChart += actualCompletedHabitTasks;
        }

        // Tính skippedTasksForChart
        long expectedTasksUpToToday = calculateTotalExpectedTasksUpToDate(userChallenge, LocalDate.now());
        skippedTasksForChart = expectedTasksUpToToday - completedTasksForChart;
        if (skippedTasksForChart < 0) {
            skippedTasksForChart = 0L;
        }

        userChallenge.setTotalExpectedTasks(totalExpectedTasks);
        userChallenge.setTotalCompletedTasks(completedTasksForChart);
        userChallenge.setSkippedTasks(skippedTasksForChart);

        if (totalExpectedTasks == 0) {
            userChallenge.setProgress(100.0);
        } else {
            double progressValue = Math.round(((double) userChallenge.getTotalCompletedTasks() / totalExpectedTasks * 100.0) * 10) / 10.0;
            userChallenge.setProgress(progressValue);
        }

        userChallengeRepository.save(userChallenge);

        checkAndCompleteChallenge(userChallenge);
    }
    @Transactional
    public Long calculateExpectedDailyTasksInPeriod(UserDaily userDaily, LocalDate startDate, LocalDate endDate) {
        Long count = 0L;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (enableToday(userDaily, current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }


    private Long calculateExpectedHabitTasksInPeriod(UserHabit userHabit, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    @Transactional
    public Long calculateTotalExpectedTasksUpToDate(UserChallenge userChallenge, LocalDate upToDate) {
        long expectedTasks = 0L;
        LocalDate challengeStartDate = userChallenge.getStartDate();
        LocalDate actualEndDate = upToDate.isBefore(userChallenge.getEndDate()) ? upToDate : userChallenge.getEndDate();

        if (challengeStartDate.isAfter(actualEndDate)) {
            return 0L;
        }

        List<UserDaily> activeUserDailies = userDailyRepository.findByUserAndDailyChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());
        List<UserHabit> activeUserHabits = userHabitRepository.findByUserAndHabitChallengeAndUnavailableFalse(userChallenge.getUser(), userChallenge.getChallenge());

        for (UserDaily ud : activeUserDailies) {
            expectedTasks += calculateExpectedDailyTasksInPeriod(ud, challengeStartDate, actualEndDate);
        }

        for (UserHabit uh : activeUserHabits) {
            expectedTasks += ChronoUnit.DAYS.between(challengeStartDate, actualEndDate) + 1;
        }
        return expectedTasks;
    }

    @Transactional
    public void updateChallengeStreak(UserChallenge userChallenge, boolean isEndDay) {
        LocalDate today = LocalDate.now();
        int completionThreshold = 100;

        UserChallengeDailyProgress userChallengeDailyProgress = this.userChallengeDPRepository
                .findByUserChallengeAndDate(userChallenge, today).orElse(null);
        boolean isCompletedToday = userChallengeDailyProgress != null &&
                userChallengeDailyProgress.getCompletionPercentage() >= completionThreshold;

        LocalDate yesterday = today.minusDays(1);
        boolean wasCompletedYesterday = false;
        if (!yesterday.isBefore(userChallenge.getStartDate()) && !yesterday.isAfter(userChallenge.getEndDate())) {
            Optional<UserChallengeDailyProgress> yesterdayProgressOpt = userChallengeDPRepository
                    .findByUserChallengeAndDate(userChallenge, yesterday);
            if (yesterdayProgressOpt.isPresent()) {
                wasCompletedYesterday = yesterdayProgressOpt.get().getCompletionPercentage() >= completionThreshold;
            }
        } else if (yesterday.isBefore(userChallenge.getStartDate())) {
            wasCompletedYesterday = true;
        }

        long currentStreak = userChallenge.getStreak();

        if (!today.isBefore(userChallenge.getStartDate()) && !today.isAfter(userChallenge.getEndDate())) {
            if (isCompletedToday) {
                if (wasCompletedYesterday || today.equals(userChallenge.getStartDate())) {
                    if (!userChallenge.isCompletedToday()) {
                        currentStreak++;
                    }
                } else {
                    currentStreak = 1L;
                }
                userChallenge.setCompletedToday(true);
            } else {
                if (userChallenge.isCompletedToday()) {
                    if (currentStreak > 0) {
                        currentStreak--;
                    }
                    userChallenge.setCompletedToday(false);
                }

                if (isEndDay) {
                    User user = userChallenge.getUser();
                    if (user.getStreakProtectionCount() != null && user.getStreakProtectionCount() > 0) {
                        user.setStreakProtectionCount(user.getStreakProtectionCount() - 1);
                        userService.updateUser(user, null);
                        emailService.sendStreakProtectNotification(userChallenge, currentStreak,user.getStreakProtectionCount());
                    } else {
                        if (currentStreak > 0) {
                            emailService.sendStreakLostNotification(userChallenge, currentStreak);
                        }
                        currentStreak = 0L;
                    }
                }
            }
        }

        if (currentStreak > userChallenge.getBestStreak()) {
            userChallenge.setBestStreak(currentStreak);
        }

        userChallenge.setStreak(currentStreak);
        userChallengeRepository.save(userChallenge);
    }

    @Transactional
    public void checkAndCompleteChallenge(UserChallenge userChallenge) {

        LocalDate today = LocalDate.now();

        boolean isPastEndDate = today.isAfter(userChallenge.getEndDate());

        boolean isProgressComplete = userChallenge.getProgress() != null && userChallenge.getProgress() >= 100.0;
        if (isProgressComplete && userChallenge.getStatus() == UserChallenge.Status.COMPLETE) {
            return;
        }

        if (isPastEndDate || isProgressComplete) {
            if (userChallenge.getStatus() != UserChallenge.Status.COMPLETE) {
                userChallenge.setNotificationShown(false);
            }
            userChallenge.setStatus(UserChallenge.Status.COMPLETE);
            Long coinEarn = this.coinCalculationService.calculateChallengeCompletionReward(userChallenge.getChallenge(),userChallenge,false);
            this.userService.getCoinCompleteForCompleteChallenge(userChallenge.getUser(),coinEarn);
            userChallenge.setCoinEarn(coinEarn);

            //tính toán độ tiến bộ
            userChallenge.setEvaluateProgress(calculateOverallEvaluationScore(userChallenge));
            userChallengeRepository.save(userChallenge);

            this.emailService.sendEmailCompleteChallenge(userChallenge);

//sẽ thực hiện sau khi hoàn thành

//            đặt incomplete của các task là false cho phép xóa task
            List<UserHabit> userHabitsInChallenge = this.userHabitRepository.findByUserAndHabitChallengeAndUnavailableFalse(userChallenge.getUser(),userChallenge.getChallenge());
            userHabitsInChallenge.forEach(userHabit -> {
                userHabit.setInChallenge(false);
                userHabitRepository.save(userHabit);
            });

            List<UserDaily> userDailiesInChallenge = this.userDailyRepository.findByUserAndDailyChallengeAndUnavailableFalse(userChallenge.getUser(),userChallenge.getChallenge());
            userDailiesInChallenge.forEach(userDaily -> {
                userDaily.setInChallenge(false);
                userDailyRepository.save(userDaily);
            });
        }else{
            if(userChallenge.getStatus() == UserChallenge.Status.COMPLETE){this.userService.getCoinCompleteForCompleteChallenge(userChallenge.getUser(),-userChallenge.getCoinEarn());}
            userChallenge.setStatus(UserChallenge.Status.ACTIVE);
            userChallenge.setNotificationShown(true);
            userChallengeRepository.save(userChallenge);
        }
    }
    @Transactional
    public boolean enableToday(UserDaily userDaily, LocalDate today) {
        long daysSinceCreation = ChronoUnit.DAYS.between(userDaily.getDaily().getCreateAt(), today);
        switch (userDaily.getRepeatFrequency()) {
            case DAILY:
                return daysSinceCreation % userDaily.getRepeatEvery() == 0;
            case WEEKLY:
                long weeksSinceCreation = daysSinceCreation / 7;
                if (weeksSinceCreation % userDaily.getRepeatEvery() != 0) {
                    return false;
                }
                UserDaily.DayOfWeek todayDayOfWeek = UserDaily.DayOfWeek.valueOf(today.getDayOfWeek().toString());
                return userDaily.getRepeatDays().contains(todayDayOfWeek);
            case MONTHLY:
                long monthsSinceCreation = ChronoUnit.MONTHS.between(userDaily.getDaily().getCreateAt().withDayOfMonth(1), today.withDayOfMonth(1));
                if (monthsSinceCreation % userDaily.getRepeatEvery() != 0) {
                    return false;
                }
                return userDaily.getRepeatMonthDays().contains(today.getDayOfMonth());
            default:
                return false;
        }
    }

    @Transactional
    public Long totalTaskPresent(User user) {
        List<Todo> todos = this.todoRepository.findByUser(user);
        List<UserDaily> userDailies = this.userDailyRepository.findByUserAndNotInChallengeAndUnavailableFalse(user);
        List<UserHabit> userHabits = this.userHabitRepository.findByUserAndNotInChallengeAndUnavailableFalse(user);

        return (long)(todos.size()+userDailies.size()+userHabits.size());
    }

    @Transactional
    public double calculateOverallEvaluationScore(UserChallenge userChallenge) {
        double W1_Maintenance = 0.4;
        double W2_GoalProgress = 0.4;
        double W3_Motivation = 0.2;

        double completionRate = userChallenge.getTotalExpectedTasks() > 0 ?
                (double) userChallenge.getTotalCompletedTasks() / userChallenge.getTotalExpectedTasks() : 1.0;

        double streakScore = 0.0;
        if (userChallenge.getChallenge().getDay() > 0) {
            streakScore = (double) userChallenge.getStreak() / userChallenge.getChallenge().getDay();
            if (streakScore > 1.0) streakScore = 1.0;
        }

        double maintenanceIndex = (0.6 * completionRate) + (0.4 * streakScore);

        double goalProgressIndex = userChallenge.getProgress() / 100.0;


        List<Diary> diariesInChallenge = diaryRepository.findDiaryInChallenge(
                userChallenge.getUser(),
                userChallenge.getChallenge().getChallengeId(),
                userChallenge.getStartDate(),
                userChallenge.getEndDate()
        );
        long numberOfDiariesWritten = diariesInChallenge.size();

        double idealDiariesForMaxMotivation = userChallenge.getChallenge().getDay() / 5.0;

        double diaryContributionRatio = (idealDiariesForMaxMotivation > 0) ? (double) numberOfDiariesWritten / idealDiariesForMaxMotivation : 0.0;
        if (diaryContributionRatio > 1.0) diaryContributionRatio = 1.0;

        double baseMotivationScore = 0.5;
        double maxDiaryBonusForMotivation = 0.5;

        double motivationIndex = baseMotivationScore + (diaryContributionRatio * maxDiaryBonusForMotivation);

        if (motivationIndex > 1.0) motivationIndex = 1.0;

        double overallScore = (W1_Maintenance * maintenanceIndex) +
                (W2_GoalProgress * goalProgressIndex) +
                (W3_Motivation * motivationIndex);

        return Math.round(overallScore * 100.0);
    }
}
