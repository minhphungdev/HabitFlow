package com.example.habittracker.Service;

import com.example.habittracker.DTO.DailyDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyService {
    private final DailyRepository dailyRepository;
    private final UserService userService;
    private final UserDailyRepository userDailyRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressService challengeProgressService;
    private final UserChallengeRepository userChallengeRepository;
    private final CoinCalculationService coinCalculationService;

    public DailyService(DailyRepository dailyRepository, UserService userService, UserDailyRepository userDailyRepository, DailyHistoryRepository dailyHistoryRepository, ChallengeRepository challengeRepository, ChallengeProgressService challengeProgressService, UserChallengeRepository userChallengeRepository, CoinCalculationService coinCalculationService) {
        this.dailyRepository = dailyRepository;
        this.userService = userService;
        this.userDailyRepository = userDailyRepository;
        this.dailyHistoryRepository = dailyHistoryRepository;
        this.challengeRepository = challengeRepository;
        this.challengeProgressService = challengeProgressService;
        this.userChallengeRepository = userChallengeRepository;
        this.coinCalculationService = coinCalculationService;
    }

    public List<UserDaily> getUserDaily(User user){
        List<UserDaily>userDailies = this.dailyRepository.findUserDailiesByUserId(user.getUserId()).stream()
                .filter(userDaily -> !userDaily.isUnavailable())
                .collect(Collectors.toList());
        LocalDate today = LocalDate.now();

        for (UserDaily userDaily : userDailies) {
            userDaily.setEnabled(enableToday(userDaily, today));
            this.userDailyRepository.save(userDaily);
        }
        return userDailies;
    }

    @Transactional
    public List<UserDaily> getDailyUnCompleteEnableToday(User user){
        return this.userDailyRepository.findByUserId(user.getUserId()).stream()
                .filter(userDaily->enableToday(userDaily,LocalDate.now()))
                .filter(userDaily -> !userDaily.isCompleted())
                .filter(userDaily->!userDaily.isUnavailable())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<UserDaily> getUserDailyChallenge(User user, Challenge challenge){
        return this.userDailyRepository.findByUserAndDailyChallengeAndUnavailableFalse(user, challenge);
    }

    @Transactional
    public void createDaily(DailyDTO dailyDTO, String username) {
        User creator = userService.getUser(username);
        Challenge challenge = null;
        if(dailyDTO.getChallengeId() != null){
            challenge = challengeRepository.findById(dailyDTO.getChallengeId()).orElse(null);
        }

        //kiểm tra limit tạo thói quen (không trong thử thách)
        if(this.challengeProgressService.totalTaskPresent(creator)>=creator.getTaskLimit() && dailyDTO.getChallengeId() == null){
            throw new RuntimeException("Không thể tạo thêm bạn đã đạt giới hạn! giới hạn cho các task của bạn là: "+creator.getTaskLimit());
        }

        Daily daily = Daily.builder()
                .title(dailyDTO.getTitle())
                .description(dailyDTO.getDescription())
                .difficulty(dailyDTO.getDifficulty())
                .repeatFrequency(dailyDTO.getRepeatFrequency())
                .repeatEvery(dailyDTO.getRepeatEvery())
                .challenge(challenge)
                .createAt(LocalDate.now())
                .build();
        daily = dailyRepository.save(daily);

        UserDaily userDaily = UserDaily.builder()
                .user(creator)
                .daily(daily)
                .streak(0L)
                .difficulty(daily.getDifficulty())
                .repeatFrequency(daily.getRepeatFrequency())
                .repeatEvery(daily.getRepeatEvery())
                .repeatDays(dailyDTO.getRepeatDays())
                .repeatMonthDays(dailyDTO.getRepeatMonthDays())
                .build();
        userDailyRepository.save(userDaily);
    }

    @Transactional
    public DailyDTO getUpdateDaily(Long dailyId, String username) {
        User dailyUser = this.userService.getUser(username);
        Daily daily = this.dailyRepository.findById(dailyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        UserDaily userDaily = userDailyRepository.findByUserAndDaily(dailyUser, daily)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu"));

        boolean isPublic = false;
        if(daily.getChallenge()!=null){
            isPublic = daily.getChallenge().getIsPublic().equals(Challenge.Visibility.PUBLIC);
        }

        return DailyDTO.builder()
                .dailyId(daily.getDailyId())
                .userId(dailyUser.getUserId())
                .title(daily.getTitle())
                .description(daily.getDescription())
                .difficulty(userDaily.getDifficulty())
                .repeatFrequency(userDaily.getRepeatFrequency())
                .repeatEvery(userDaily.getRepeatEvery())
                .repeatDays(userDaily.getRepeatDays())
                .repeatMonthDays(userDaily.getRepeatMonthDays())
                .isPublic(isPublic)
                .isInChallenge(userDaily.isInChallenge())
                .challengeId(daily.getChallenge() != null? daily.getChallenge().getChallengeId() : null)
                .challengeTitle(daily.getChallenge() != null? daily.getChallenge().getTitle() : "Không có")
                .build();
    }

    @Transactional
    public void updateDaily(DailyDTO dailyDTO, String username) {
            User user = userService.getUser(username);

        Daily daily = dailyRepository.findById(dailyDTO.getDailyId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        UserDaily userDaily = userDailyRepository.findByUserAndDaily(user, daily)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu"));
        Challenge challenge = null;
        if(dailyDTO.getChallengeId()!=null){
            challenge = challengeRepository.findById(dailyDTO.getChallengeId()).orElse(null);
        }

        if(this.challengeProgressService.totalTaskPresent(user)>=user.getTaskLimit() && dailyDTO.getChallengeId() == null){
            throw new RuntimeException("Không thể cập nhật bạn đã đạt giới hạn! giới hạn cho các thói quen không trong thử thách của bạn là: "+user.getTaskLimit());
        }

        //Không thể chỉnh sửa thói quen đã đăng lên cộng đồng
        if(daily.getChallenge()!=null){
            if(!daily.getChallenge().getIsPublic().equals(Challenge.Visibility.PUBLIC)){
                daily.setTitle(dailyDTO.getTitle());
                daily.setDescription(dailyDTO.getDescription());
                daily.setChallenge(challenge);
            }
        }else{
            if(challenge!=null && !challenge.getIsPublic().equals(Challenge.Visibility.PUBLIC)){
                daily.setTitle(dailyDTO.getTitle());
                daily.setDescription(dailyDTO.getDescription());
                daily.setChallenge(challenge);
            }
        }
        this.dailyRepository.save(daily);

        //cập nhật lại thử thách khi thêm thói quen vào
        UserChallenge userChallenge = this.userChallengeRepository.findByUserAndChallenge(user, daily.getChallenge()).orElse(null);;
        if(userChallenge != null && userChallenge.getStatus() == UserChallenge.Status.ACTIVE){
            this.challengeProgressService.calculateAndSaveDailyProgress(userChallenge.getUserChallengeId(),LocalDate.now());
            this.challengeProgressService.recalculateUserChallengeProgress(userChallenge);
            this.challengeProgressService.updateChallengeStreak(userChallenge,false);
        }

        userDaily.setDifficulty(dailyDTO.getDifficulty());
        userDaily.setRepeatFrequency(dailyDTO.getRepeatFrequency());
        userDaily.setRepeatEvery(dailyDTO.getRepeatEvery());
        userDaily.setRepeatDays(dailyDTO.getRepeatDays());
        userDaily.setRepeatMonthDays(dailyDTO.getRepeatMonthDays());
        this.userDailyRepository.save(userDaily);
    }

    @Transactional
    public void deleteDaily(Long dailyId, String username) {
        User user = userService.getUser(username);
        Daily daily = this.dailyRepository.findById(dailyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        UserDaily userDaily = this.userDailyRepository.findByUserAndDaily(user, daily)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu"));

        if (daily == null && userDaily == null) {
            throw new RuntimeException("Có lỗi xảy ra khi xóa! Không tìm thấy thói quen để xóa");
        }

        List<DailyHistory> dailyHistories = this.dailyHistoryRepository.findAllByUserDaily(userDaily);

        if(daily.getChallenge() != null){
            UserChallenge userChallenge = this.userChallengeRepository.findByUserAndChallenge(user,daily.getChallenge()).orElseThrow(()->new RuntimeException("Không tìm thấy dữ liệu để xóa!"));
            if(userChallenge.getStatus() == UserChallenge.Status.COMPLETE){
                userDaily.setUnavailable(true);
                this.userDailyRepository.save(userDaily);
            }else{
                this.dailyHistoryRepository.deleteAll(dailyHistories);
                this.dailyRepository.delete(daily);
                this.userDailyRepository.delete(userDaily);
            }
        }else{
            this.dailyHistoryRepository.deleteAll(dailyHistories);
            this.dailyRepository.delete(daily);
            this.userDailyRepository.delete(userDaily);
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
    public DailyDTO dailyTaskUpdate(String username, Long dailyId, String status) {
        Daily daily = this.dailyRepository.findById(dailyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        User user = this.userService.getUser(username);
        UserDaily userDaily = this.userDailyRepository.findByUserAndDaily(user, daily)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu"));
        DailyDTO dailyDTO = new DailyDTO();
        LocalDate today = LocalDate.now();

        DailyHistory dailyHistory = this.dailyHistoryRepository.findDailyHistory(userDaily, today)
                .orElseGet(() -> new DailyHistory().builder()
                        .userDaily(userDaily)
                        .date(today)
                        .streak(userDaily.getStreak())
                        .isCompleted(false)
                        .coinEarned(0L)
                        .build());

        if ("checked".equals(status)) {
            userDaily.setCompleted(true);
            dailyHistory.setCompleted(true);
            userDaily.setStreak(userDaily.getStreak() + 1);

            Long coinEarn = this.coinCalculationService.calculateDailyCoins(userDaily,daily.getChallenge() != null && userDaily.isUnavailable());
            Long actualCoinEarn = this.userService.getCoinComplete(user,coinEarn);
            dailyHistory.setCoinEarned(actualCoinEarn);
            String message;
            if(actualCoinEarn > 0){
                message = "+"+actualCoinEarn;
            }else {
                message = "Bạn đã đạt giới hạn xu ngày hôm nay";
            }
            dailyDTO.setUserCoinMessage(message);
            dailyDTO.setCoinEarned(coinEarn);
        } else if ("unchecked".equals(status)) {
            userDaily.setCompleted(false);
            dailyHistory.setCompleted(false);
            userDaily.setStreak(userDaily.getStreak() - 1);

            Long coinBack = this.dailyHistoryRepository.findCoinEarnedByUserDailyAndUser(userDaily,today);
            Long actualCoinBack = this.userService.getCoinComplete(user,-coinBack);
            dailyDTO.setCoinEarned(actualCoinBack);
            String message;
            if(actualCoinBack<0){
                message = ""+actualCoinBack;
            }else{
                message ="";
            }
            dailyDTO.setUserCoinMessage(message);
            dailyHistory.setCoinEarned(0L);
        }

        this.userDailyRepository.save(userDaily);
        dailyHistory.setStreak(userDaily.getStreak());
        this.dailyHistoryRepository.save(dailyHistory);

        if(daily.getChallenge() != null) {
            UserChallenge userChallenge = this.userChallengeRepository.findByUserAndChallenge(user, daily.getChallenge()).orElse(null);;
            if(userChallenge != null && userChallenge.getStatus() == UserChallenge.Status.ACTIVE){
                this.challengeProgressService.calculateAndSaveDailyProgress(userChallenge.getUserChallengeId(),today);
                this.challengeProgressService.recalculateUserChallengeProgress(userChallenge);
                this.challengeProgressService.updateChallengeStreak(userChallenge,false);
            }
        }

        dailyDTO.setStreak(userDaily.getStreak());
        dailyDTO.setCompleted(userDaily.isCompleted());

        return dailyDTO;
    }

    @Transactional
    public void resetDaily(){
        List<UserDaily> userDailies = this.userDailyRepository.findAll();
        LocalDate today = LocalDate.now();
        for (UserDaily userDaily : userDailies) {
            if (enableToday(userDaily, today)) {
                DailyHistory lastDailyHistory = findLastEnableHistory(userDaily, today);
                DailyHistory dailyHistory = this.dailyHistoryRepository.findDailyHistory(userDaily, today)
                        .orElseGet(() -> new DailyHistory().builder()
                                .userDaily(userDaily)
                                .date(today)
                                .streak(0L)
                                .isCompleted(false)
                                .build());
                if (dailyHistory != null && lastDailyHistory != null) {
                    if (lastDailyHistory.isCompleted() && dailyHistory.isCompleted()) {
                        userDaily.setStreak(userDaily.getStreak());
                    } else if (lastDailyHistory.isCompleted() && !dailyHistory.isCompleted()) {
                        userDaily.setStreak(0L);
                    } else if (!lastDailyHistory.isCompleted() && dailyHistory.isCompleted()) {
                        userDaily.setStreak(1L);
                    } else if (!lastDailyHistory.isCompleted() && !dailyHistory.isCompleted()) {
                        userDaily.setStreak(0L);
                    }
                } else if (dailyHistory != null) {
                    if (dailyHistory.isCompleted()) {
                        userDaily.setStreak(userDaily.getStreak());
                    } else {
                        userDaily.setStreak(0L);
                    }
                }
                userDaily.setCompleted(false);

                this.userDailyRepository.save(userDaily);
                this.dailyHistoryRepository.save(dailyHistory);
            }
        }
    }

    @Transactional
    public void setDailyHistoryNewDay(){
        List<UserDaily> userDailies = this.userDailyRepository.findAll();
        LocalDate today = LocalDate.now();
        for (UserDaily userDaily : userDailies) {
            if (enableToday(userDaily, today)) {
                DailyHistory dailyHistory = this.dailyHistoryRepository.findDailyHistory(userDaily, today)
                        .orElseGet(() -> new DailyHistory().builder()
                                .userDaily(userDaily)
                                .date(today)
                                .streak(0L)
                                .isCompleted(false)
                                .coinEarned(0L)
                                .build());

                this.dailyHistoryRepository.save(dailyHistory);
            }
        }
    }

    public List<DailyDTO> getDailiesByChallengeId(Long challengeId) {
        List<Daily> dailies = dailyRepository.findByChallengeId(challengeId);
        return dailies.stream().map(daily -> DailyDTO.builder()
                .dailyId(daily.getDailyId())
                .title(daily.getTitle())
                .description(daily.getDescription())
                .difficulty(daily.getDifficulty())
                .repeatFrequency(daily.getRepeatFrequency())
                .repeatEvery(daily.getRepeatEvery())
                .build()).collect(Collectors.toList());
    }

    public void unlinkDailyFromChallenge(Long dailyId) {
        Daily daily = dailyRepository.findById(dailyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thói quen hàng ngày"));
        daily.setChallenge(null);
        dailyRepository.save(daily);
    }

    public DailyHistory findLastEnableHistory(UserDaily userDaily, LocalDate today){
        List<DailyHistory> dailyHistories = this.dailyHistoryRepository.findAllByUserDailyOrderByDateDesc(userDaily);
        for (DailyHistory dailyHistory : dailyHistories) {
            if (dailyHistory.getDate().isBefore(today) && enableToday(userDaily, dailyHistory.getDate())) {
                return dailyHistory;
            }
        }
        return null;
    }

    @Transactional
    public long countCompleteDaily(User user) {
        List<UserDaily> userDailies = this.userDailyRepository.findByUserId(user.getUserId());
        return userDailies.stream().mapToLong(userDaily -> this.dailyHistoryRepository.countCompleteDaily(userDaily)).sum();
    }


}
