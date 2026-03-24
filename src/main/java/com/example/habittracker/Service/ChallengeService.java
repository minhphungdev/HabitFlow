package com.example.habittracker.Service;

import com.example.habittracker.DTO.ChallengeDTO;
import com.example.habittracker.DTO.DailyDTO;
import com.example.habittracker.DTO.DailyProgressDTO;
import com.example.habittracker.DTO.HabitDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final HabitService habitService;
    private final DailyService dailyService;
    private final UserChallengeRepository userChallengeRepository;
    private final UserChallengeDPRepository userChallengeDPRepository;
    private final ChallengeProgressService challengeProgressService;
    private final UserHabitRepository userHabitRepository;
    private final UserDailyRepository userDailyRepository;
    private final HabitHistoryRepository habitHistoryRepository;
    private final CoinCalculationService coinCalculationService;
    private final UserService userService;
    private final EmailService emailService;

    public ChallengeService(ChallengeRepository challengeRepository, HabitService habitService, DailyService dailyService, UserChallengeRepository userChallengeRepository, UserChallengeDPRepository userChallengeDPRepository, ChallengeProgressService challengeProgressService, UserHabitRepository userHabitRepository, UserDailyRepository userDailyRepository, HabitHistoryRepository habitHistoryRepository, CoinCalculationService coinCalculationService, UserService userService, EmailService emailService) {
        this.challengeRepository = challengeRepository;
        this.habitService = habitService;
        this.dailyService = dailyService;
        this.userChallengeRepository = userChallengeRepository;
        this.userChallengeDPRepository = userChallengeDPRepository;
        this.challengeProgressService = challengeProgressService;
        this.userHabitRepository = userHabitRepository;
        this.userDailyRepository = userDailyRepository;
        this.habitHistoryRepository = habitHistoryRepository;
        this.coinCalculationService = coinCalculationService;
        this.userService = userService;
        this.emailService = emailService;
    }
    @Transactional
    public List<UserChallenge> getChallenges(Long userId) {
        return this.userChallengeRepository.findUnCompleteChallengeByUsersId(userId).get();
    }

    @Transactional
    public UserChallenge getLongestStreakUserChallenges(Long userId) {
        return this.challengeRepository.findAllByUser(userId).stream().max(Comparator.comparing(UserChallenge::getBestStreak)).orElse(null);
    }

    @Transactional
    public List<UserChallenge> getChallengesOwner(Long userId){
        return this.challengeRepository.findUserChallengeOwner(userId).get();
    }

    @Transactional
    public Challenge getChallengeById(Long challengeId) {
        return this.challengeRepository.findById(challengeId).get();
    }

    @Transactional
    public UserChallenge getUserChallenge(User user, Challenge challenge){
        return this.userChallengeRepository.findByUserAndChallenge(user,challenge).orElseThrow(()->new RuntimeException("Không tìm thấy dữ liệu thử thách!"));
    }


    @Transactional
    //lấy list uc mà đag active và uc hoàn thành thử thach bằng progress 100
    public List<UserChallenge> getValidChallenges(Long userId) {
        List<UserChallenge> userChallenges = this.challengeRepository.findChallengeByUserId(userId).get();
        LocalDate today = LocalDate.now();
        return userChallenges.stream()
                .filter(uc->{
                        if (uc.getStatus() == UserChallenge.Status.ACTIVE) {
                            return !today.isAfter(uc.getEndDate());
                        }else if (uc.getStatus() == UserChallenge.Status.COMPLETE) {
                            if (uc.getProgress() != null && uc.getProgress() >= 100.0) {
                                return !today.isAfter(uc.getEndDate());
                            }
                        }return false;
                        }
                     ).collect(Collectors.toList());
    }
    @Transactional
    public List<UserChallenge> getCompletedChallengesForNotification(User user) {
        return userChallengeRepository.findByUserAndStatusAndIsNotificationShownFalse(user, UserChallenge.Status.COMPLETE);
    }

    @Transactional
    public List<UserChallenge> getAllActiveChallenges(User user){
        return userChallengeRepository.findByUserAndChallengeIsActive(user);
    }

    @Transactional
    public ChallengeDTO getUserChallengeDetail(User user, Long challengeId) {
        Challenge challenge = this.challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Challenge với ID: " + challengeId));

        UserChallenge userChallenge = this.userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy UserChallenge của người dùng này cho Challenge đó."));

        List<DailyProgressDTO> dailyProgressDTOs = userChallenge.getDailyProgresses()
                .stream()
                .map(dp -> DailyProgressDTO.builder()
                        .date(dp.getDate())
                        .completionPercentage(dp.getCompletionPercentage())
                        .build()).collect(Collectors.toList());

        return ChallengeDTO.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .progress(userChallenge.getProgress())
                .bestStreak(userChallenge.getBestStreak())
                .totalCompletedTasks(userChallenge.getTotalCompletedTasks())
                .completedTasks(userChallenge.getTotalCompletedTasks())
                .skippedTasks(userChallenge.getSkippedTasks())
                .completedTasksList(userChallenge.getCompletedTasksList())
                .dailyProgresses(dailyProgressDTOs)
                .completedTasksList(userChallenge.getCompletedTasksList())
                .status(userChallenge.getStatus())
                .startDate(userChallenge.getStartDate())
                .endDate(userChallenge.getEndDate())
                .day(challenge.getDay())
                .totalExpectedTasks(userChallenge.getTotalExpectedTasks())
                .evaluationPercentage(userChallenge.getEvaluateProgress())
                .build();
    }
    @Transactional
    public ChallengeDTO getChallengeDTOById(Long challengeId,User user) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thử thách"));
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user,challenge).get();

        List<HabitDTO> habits = habitService.getHabitsByUser_ChallengeId(challengeId);
        List<DailyDTO> dailies = dailyService.getDailiesByChallengeId(challengeId);

        return ChallengeDTO.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .challengeParticipant(challenge.getParticipantCount())
                .day(challenge.getDay())
                .endDate(userChallenge.getEndDate() != null ? userChallenge.getEndDate() : null)
                .isPublic(challenge.getIsPublic())
                .habits(habits)
                .dailies(dailies)
                .bestStreak(userChallenge.getBestStreak())
                .progress(userChallenge.getProgress())
                .build();
    }

    @Transactional
    public void createChallenge(ChallengeDTO challengeDTO, User creator) {

        if (challengeDTO.getTitle() == null || challengeDTO.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề thử thách không được để trống!");
        }
        if (challengeDTO.getEndDate() == null || challengeDTO.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày kết thúc không hợp lệ!");
        }

        LocalDate startDate = LocalDate.now();

        if (challengeDTO.getDay() < 5) {
            throw new RuntimeException("Thời gian thực hiện phải ít nhất 5 ngày!");
        }

        if(getValidChallenges(creator.getUserId()).size()>=creator.getChallengeLimit()){
            throw new RuntimeException("Bạn đã đạt giới hạn tạo thử thách. Giới hạn hiện tại của bạn là: "+creator.getChallengeLimit());
        }

        Challenge challenge = Challenge.builder()
                .creatorId(creator.getUserId())
                .title(challengeDTO.getTitle())
                .description(challengeDTO.getDescription())
                .day(challengeDTO.getDay())
                .isPublic(Challenge.Visibility.PRIVATE)
                .participantCount(1L)
                .build();
        challenge = challengeRepository.save(challenge);


        if (challengeDTO.getHabits() != null) {
            for (HabitDTO habitDTO : challengeDTO.getHabits()) {
                habitDTO.setChallengeId(challenge.getChallengeId());
                habitService.save(habitDTO, creator.getEmail());
            }
        }

        if (challengeDTO.getDailies() != null) {
            for (DailyDTO dailyDTO : challengeDTO.getDailies()) {
                dailyDTO.setChallengeId(challenge.getChallengeId());
                dailyService.createDaily(dailyDTO, creator.getEmail());
            }
        }

        UserChallenge userChallenge = UserChallenge.builder()
                .user(creator)
                .challenge(challenge)
                .progress(0.0)
                .startDate(startDate)
                .endDate(challengeDTO.getEndDate())
                .status(UserChallenge.Status.ACTIVE)
                .streak(0L)
                .bestStreak(0L)
                .daysSinceStart(ChronoUnit.DAYS.between(startDate, LocalDate.now().plusDays(1)))
                .totalCompletedTasks(0L)
                .totalExpectedTasks(0L)
                .skippedTasks(0L)
                .completedTasksList(new ArrayList<>())
                .build();
        userChallengeRepository.save(userChallenge);

        challengeProgressService.calculateAndSaveDailyProgress(userChallenge.getUserChallengeId(),startDate);
        challengeProgressService.recalculateUserChallengeProgress(userChallenge);

        //cập nhật coinExpect
        challenge.setCoinEarnExpect(this.coinCalculationService.calculateChallengeCompletionReward(challenge,userChallenge,true));
        this.challengeRepository.save(challenge);
    }

    @Transactional
    public void updateChallenge(ChallengeDTO challengeDTO, User creator) {
        Challenge challenge = challengeRepository.findById(challengeDTO.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Không tìm tháy dữ liệu thử thách!"));
        UserChallenge userChallenge = userChallengeRepository.findByChallenge(challenge)
                .orElseThrow(() -> new RuntimeException("Không tìm tháy dữ liệu thử thách!"));

        if (challenge.getIsPublic() == Challenge.Visibility.PUBLIC) {
            throw new RuntimeException("Không thể chỉnh sửa thử thách đã được công khai.");
        }

        if (challengeDTO.getTitle() == null || challengeDTO.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề thử thách không được để trống!");
        }
        if (challengeDTO.getEndDate() == null || challengeDTO.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày kết thúc không hợp lệ!");
        }

        if (challengeDTO.getDay() < 5) {
            throw new RuntimeException("Thời gian thực hiện phải ít nhất 5 ngày!");
        }

        if(challengeDTO.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Thời gian kết thức phải trước thời gian hiện tại!");
        }

//        Cập nhật Challenge
        challenge.setTitle(challengeDTO.getTitle());
        challenge.setDescription(challengeDTO.getDescription());
        challenge.setDay(challengeDTO.getDay());
        challengeRepository.save(challenge);

        // Cập nhật UserChallenge
        userChallenge.setEndDate(challengeDTO.getEndDate());
        userChallengeRepository.save(userChallenge);


//        xử lý Habits
        List<HabitDTO> existingHabits = habitService.getHabitsByUser_ChallengeId(challenge.getChallengeId());
        List<Long> newHabitIds = challengeDTO.getHabits().stream()
                .map(HabitDTO::getHabitId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

//        xóa các Habit không còn trong danh sách mới
        for (HabitDTO existingHabit : existingHabits) {
            if (!newHabitIds.contains(existingHabit.getHabitId())) {
                habitService.unlinkHabitFromChallenge(existingHabit.getHabitId());
            }
        }

//        thêm các Habit mới
        for (HabitDTO habitDTO : challengeDTO.getHabits()) {
            if (habitDTO.getHabitId() == null) {
                habitDTO.setChallengeId(challenge.getChallengeId());
                habitService.save(habitDTO, creator.getEmail());
            }
        }

//        xử lý Dailies
        List<DailyDTO> existingDailies = dailyService.getDailiesByChallengeId(challenge.getChallengeId());
        List<Long> newDailyIds = challengeDTO.getDailies().stream()
                .map(DailyDTO::getDailyId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

//       xóa các Daily không còn trong danh sách mới
        for (DailyDTO existingDaily : existingDailies) {
            if (!newDailyIds.contains(existingDaily.getDailyId())) {
                dailyService.unlinkDailyFromChallenge(existingDaily.getDailyId());
            }
        }

//        thêm các Daily mới
        for (DailyDTO dailyDTO : challengeDTO.getDailies()) {
            if (dailyDTO.getDailyId() == null) {
                dailyDTO.setChallengeId(challenge.getChallengeId());
                dailyService.createDaily(dailyDTO, creator.getEmail());
            }
        }
        challengeProgressService.calculateAndSaveDailyProgress(userChallenge.getUserChallengeId(),LocalDate.now());
        challengeProgressService.recalculateUserChallengeProgress(userChallenge);
    }

    @Transactional
    public void deleteChallenge(Long challengeId, User creator) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(()->new RuntimeException("Không tìm thấy thử thách! Xóa thất bại"));
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(creator,challenge).orElseThrow(()->new RuntimeException("Lỗi khi tìm dữ liệu! Xóa thất bại"));

        if(challenge.getIsPublic() == Challenge.Visibility.PRIVATE){
            List<Habit> habits = this.challengeRepository.findHabitsByChallengeId(challenge.getChallengeId());
            habits.forEach(habit -> {this.habitService.unlinkHabitFromChallenge(habit.getHabitId());});

            List<Daily> dailies = this.challengeRepository.findDailiesByChallengeId(challenge.getChallengeId());
            dailies.forEach(daily -> {this.dailyService.unlinkDailyFromChallenge(daily.getDailyId());});

            this.userChallengeDPRepository.deleteAllByUserChallenge(userChallenge);
            this.userChallengeRepository.delete(userChallenge);
            this.challengeRepository.delete(challenge);
        }else{
            this.userChallengeDPRepository.deleteAllByUserChallenge(userChallenge);
            this.userChallengeRepository.delete(userChallenge);
        }

    }

    //community----------------------------------------------------------------------------------------------------------
    @Transactional
    public List<UserChallenge> getSharedChallenge(){
        return this.userChallengeRepository.findByChallengePublic();
    }

    @Transactional
    public List<UserChallenge> getUserCompleteChallenge(User user){
        return this.userChallengeRepository.findByUserAndCompleted(user);
    }

    @Transactional
    public List<UserChallenge> getAllCompleteChallenge(User user){
        return this.userChallengeRepository.findAllByUserAndCompleted(user);
    }

    @Transactional
    public void shareChallenge(User user, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thử thách"));

        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new RuntimeException("Lấy dữ liệu thử thách thất bại"));

        if (!challenge.getCreatorId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền chia sẻ thử thách này");
        }
        if (userChallenge.getStatus() != UserChallenge.Status.COMPLETE) {
            throw new RuntimeException("Thử Thách cần hoàn thành trước khi chia sẻ");
        }

        challenge.setIsPublic(Challenge.Visibility.PENDING);
        challengeRepository.save(challenge);
    }

    @Transactional
    public void joinChallenge(User user, Challenge challenge) {
        if (challenge.getIsPublic() != Challenge.Visibility.PUBLIC) {
            throw new RuntimeException("Thử thách chưa được chia sẻ");
        }

        // Kiểm tra xem người dùng đã tham gia chưa
        Optional<UserChallenge> existingUserChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge);
        if (existingUserChallenge.isPresent()) {
            throw new RuntimeException("Bạn đã tham gia thử thách này rồi!");
        }

        // Tạo UserChallenge mới
        UserChallenge userChallenge = UserChallenge.builder()
                .user(user)
                .challenge(challenge)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(challenge.getDay()))
                .status(UserChallenge.Status.ACTIVE)
                .streak(0L)
                .bestStreak(0L)
                .progress(0.0)
                .totalCompletedTasks(0L)
                .daysSinceStart(ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now()))
                .totalExpectedTasks(0L)
                .skippedTasks(0L)
                .completedTasksList(new ArrayList<>())
                .build();
        userChallengeRepository.save(userChallenge);

        // Sao chép danh sách Habit vào UserHabit
        for (Habit habit : challenge.getHabits()) {
            UserHabit userHabit = UserHabit.builder()
                    .user(user)
                    .habit(habit)
                    .positiveCount(0L)
                    .negativeCount(0L)
                    .targetCount(habit.getTargetCount())
                    .unavailable(false)
                    .difficulty(habit.getDifficulty())
                    .build();
            if(habit.getType() == Habit.HabitType.NEGATIVE){
                userHabit.setCompleted(true);
            }else{
                userHabit.setCompleted(false);
            }
            userHabit.setInChallenge(true);
            userHabitRepository.save(userHabit);
            if (habit.getType() == Habit.HabitType.NEGATIVE) {
                HabitHistory initialHistory = HabitHistory.builder()
                        .userHabit(userHabit)
                        .date(LocalDate.now())
                        .isCompleted(true)
                        .positiveCount(0L)
                        .negativeCount(0L)
                        .coinEarned(0L)
                        .build();
                this.habitHistoryRepository.save(initialHistory);
            };
        }

        // Sao chép danh sách Daily vào UserDaily
        for (Daily daily : challenge.getDailies()) {
            UserDaily userDaily = UserDaily.builder()
                    .user(user)
                    .daily(daily)
                    .streak(0L)
                    .isCompleted(false)
                    .isEnabled(true)
                    .difficulty(daily.getDifficulty())
                    .repeatFrequency(daily.getRepeatFrequency())
                    .repeatEvery(daily.getRepeatEvery())
                    .build();
            if(daily.getRepeatFrequency() == Daily.RepeatFrequency.WEEKLY){
//                userDaily.setRepeatDays(new HashSet<>(Arrays.asList(UserDaily.DayOfWeek.values())));
                userDaily.setRepeatDays(new HashSet<>(daily.getUserDailies().get(0).getRepeatDays()));
            } else if (daily.getRepeatFrequency() == Daily.RepeatFrequency.MONTHLY) {
//                userDaily.setRepeatMonthDays(new HashSet<>(Arrays.asList(1, 2, 3)));
                userDaily.setRepeatMonthDays(new HashSet<>(daily.getUserDailies().get(0).getRepeatMonthDays()));
            }
            userDaily.setInChallenge(true);

            userDailyRepository.save(userDaily);
        }


        challengeProgressService.calculateAndSaveDailyProgress(userChallenge.getUserChallengeId(),LocalDate.now());
        challengeProgressService.recalculateUserChallengeProgress(userChallenge);
        // Cập nhật participantCount
        challenge.setParticipantCount(challenge.getParticipantCount() + 1);
        challengeRepository.save(challenge);
    }

    @Transactional
    public void CalChallengeProgressEndDay(){
        LocalDate today = LocalDate.now();
        List<UserChallenge> userChallenge = this.userChallengeRepository.findAll();

        List<UserChallenge> activeUserChallenge = userChallenge.stream()
                .filter(uc->uc.getStatus() == UserChallenge.Status.ACTIVE)
                .filter(uc -> !today.isAfter(uc.getEndDate()) && !today.isBefore(uc.getStartDate()) )
                .collect(Collectors.toList());
        for(UserChallenge uc : activeUserChallenge){
            this.challengeProgressService.calculateAndSaveDailyProgress(uc.getUserChallengeId(),LocalDate.now());
            this.challengeProgressService.recalculateUserChallengeProgress(uc);
            this.challengeProgressService.updateChallengeStreak(uc,true);

            //cập nhật iscompleteToday của userchallenge
            uc.setDaysSinceStart(ChronoUnit.DAYS.between(uc.getStartDate(), LocalDate.now().plusDays(2)));
            uc.setCompletedToday(false);
            this.userChallengeRepository.save(uc);
        }
    }

    @Transactional
    public void calChallengeProgressNewDay(){
        LocalDate today = LocalDate.now();
        List<UserChallenge> userChallenge = this.userChallengeRepository.findAll();

        List<UserChallenge> activeUserChallenge = userChallenge.stream()
                .filter(uc->uc.getStatus() == UserChallenge.Status.ACTIVE)
                .filter(uc -> !today.isAfter(uc.getEndDate()) && !today.isBefore(uc.getStartDate()) )
                .collect(Collectors.toList());
        for(UserChallenge uc : activeUserChallenge){
            this.challengeProgressService.calculateAndSaveDailyProgress(uc.getUserChallengeId(),LocalDate.now());
        }
    }
    @Transactional
    public void checkChallengeCompleted(){
        List<UserChallenge> activeChallenges = userChallengeRepository.findByStatus(UserChallenge.Status.ACTIVE);
        activeChallenges.forEach(challengeProgressService::checkAndCompleteChallenge);
    }

//    dashBoard-------------------------------------------------------------------------------------
    @Transactional
    public List<UserChallenge> getPendingChallenges(){
        return this.userChallengeRepository.findByChallengePending();
    }

    @Transactional
    public void ChallengeApprove(Long challengeId, boolean approve){
        Challenge challenge = this.challengeRepository.findById(challengeId).orElseThrow(()->new RuntimeException("Không tìm thấy thử thách!"));
        challenge.setIsPublic(approve?Challenge.Visibility.PUBLIC:Challenge.Visibility.PRIVATE);
        this.challengeRepository.save(challenge);
        User creator = this.userService.getUserById(challenge.getCreatorId());
        if(creator != null){
            if(approve){
                this.emailService.sendChallengeApprovedEmail(creator, challenge);
            }else {
                emailService.sendChallengeRejectedEmail(creator, challenge);
            }
        }
    }

}
