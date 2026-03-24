package com.example.habittracker.Service;

import com.example.habittracker.DTO.AchievementDTO;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ChallengeService challengeService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AchievementService(AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository, ChallengeService challengeService, UserService userService, UserRepository userRepository, EmailService emailService) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.challengeService = challengeService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Achievement getAchievementById(Long id) {
        return this.achievementRepository.getAchievementById(id);
    }

    @Transactional
    public AchievementDTO getUpdateAchievementById(Long id) {
        Achievement achievement = this.achievementRepository.getAchievementById(id);
        return AchievementDTO.builder()
                .achievementId(achievement.getAchievementId())
                .achievementTitle(achievement.getTitle())
                .achievementDescription(achievement.getDescription())
                .bonusChallenge(achievement.getChallengeBonus())
                .bonusTask(achievement.getTaskBonus())
                .requiredChallenge(achievement.getRequiredChallenge())
                .requiredTask(achievement.getRequiredTask())
                .icon(achievement.getIcon())
                .color(achievement.getColor())
                .coinBonus(achievement.getCoinBonus())
                .build();
    }

    @Transactional
    public void saveUserAchievement(UserAchievement userAchievement) {
        this.userAchievementRepository.save(userAchievement);
    }

    @Transactional
    public List<UserAchievement> getAchievementReceive(User user){
        return this.userAchievementRepository.getUserAchievementReceiveTodayAndNotificationFalse(LocalDate.now(),user);
    }

    @Transactional
    public List<UserAchievement> getUserAchievementReceive(User user){
        return this.userAchievementRepository.getUserAchievementReceive(user);
    }

    @Transactional
    public void receiveAchievement(User user) {

        long totalCompletedTask = this.userService.getTaskComplete(user,true);

        long totalChallengeValid= this.challengeService.getUserCompleteChallenge(user).size();

        List<Achievement> achievements = this.achievementRepository.findAll();

        for (Achievement achievement : achievements) {

            UserAchievement userAchievement = this.achievementRepository.getUserAchievemenByUserAchievement(user,achievement).orElse(null);
            if(userAchievement!=null)continue;

            if(totalChallengeValid >0 && totalCompletedTask > 0){
                if(achievement.getRequiredChallenge()>0 && achievement.getRequiredTask()>0){
                    if(totalChallengeValid >= achievement.getRequiredChallenge() && totalCompletedTask >= achievement.getRequiredTask()){
                        UserAchievement newUserAchievement = UserAchievement.builder()
                                .user(user)
                                .achievement(achievement)
                                .earnedDate(LocalDateTime.now())
                                .isNotification(false)
                                .build();
                        this.userAchievementRepository.save(newUserAchievement);

                        this.emailService.sendEmailReceiveAchievement(newUserAchievement);

                        user.setChallengeLimit(achievement.getChallengeBonus()+ user.getChallengeLimit());
                        user.setTaskLimit(achievement.getTaskBonus()+ user.getTaskLimit());
                        user.setCoins(achievement.getCoinBonus()+user.getCoins());
                        this.userRepository.save(user);
                    }
                }
            }
            if(achievement.getRequiredChallenge()==0){
                if (totalCompletedTask>=achievement.getRequiredTask()) {
                    UserAchievement newUserAchievement = UserAchievement.builder()
                            .user(user)
                            .achievement(achievement)
                            .earnedDate(LocalDateTime.now())
                            .build();
                    this.userAchievementRepository.save(newUserAchievement);

                    this.emailService.sendEmailReceiveAchievement(newUserAchievement);

                    user.setTaskLimit(achievement.getTaskBonus()+ user.getTaskLimit());
                    user.setCoins(achievement.getCoinBonus()+user.getCoins());
                    this.userRepository.save(user);
                }
            }

            if(achievement.getRequiredTask()==0){
                if (totalChallengeValid>=achievement.getRequiredChallenge()){
                    UserAchievement newUserAchievement = UserAchievement.builder()
                            .user(user)
                            .achievement(achievement)
                            .earnedDate(LocalDateTime.now())
                            .build();
                    this.userAchievementRepository.save(newUserAchievement);

                    this.emailService.sendEmailReceiveAchievement(newUserAchievement);

                    user.setChallengeLimit(achievement.getChallengeBonus()+ user.getChallengeLimit());
                    user.setCoins(achievement.getCoinBonus()+user.getCoins());
                    this.userRepository.save(user);
                }
            }

        }
    }

    public Page<Achievement> getAchievementsBySearch(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return achievementRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            return achievementRepository.findAll(pageable);
        }
    }

    @Transactional
    public List<Achievement> getAllAchievement() {
        return this.achievementRepository.findAll();
    }

    @Transactional
    public List<AchievementDTO> getAllUserAchievementToday() {
        List<UserAchievement> userAchievements = this.userAchievementRepository.findAllAllUserAchievementReceiveToday(LocalDate.now());
        return userAchievements.stream().map(userAchievement->{
            UserDTO userDTO = UserDTO.builder()
                    .username(userAchievement.getUser().getUserName())
                    .avatar(userAchievement.getUser().getAvatar())
                    .build();
            Duration duration = Duration.between(userAchievement.getEarnedDate(), LocalDateTime.now());
            return AchievementDTO.builder()
                    .user(userDTO)
                    .achievementTitle(userAchievement.getAchievement().getTitle())
                    .durationAchievement(duration.toHours())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addNewAchievement(AchievementDTO achievementDTO) {
        if(achievementDTO.getAchievementTitle()==null && achievementDTO.getAchievementTitle().isEmpty()){
            throw new RuntimeException("Tên thành tựu không được trống!");
        }

        Achievement achievement = Achievement.builder()
                .title(achievementDTO.getAchievementTitle())
                .description(achievementDTO.getAchievementDescription())
                .requiredChallenge(achievementDTO.getRequiredChallenge())
                .requiredTask(achievementDTO.getRequiredTask())
                .challengeBonus(achievementDTO.getBonusChallenge())
                .taskBonus(achievementDTO.getBonusTask())
                .icon(achievementDTO.getIcon())
                .coinBonus(achievementDTO.getCoinBonus())
                .color(achievementDTO.getColor())
                .isAvailable(true)
                .build();

        this.achievementRepository.save(achievement);
    }

    @Transactional
    public void updateAchievement(AchievementDTO achievementDTO) {
         Achievement achievement = this.achievementRepository.getAchievementById(achievementDTO.getAchievementId());

        if(achievement == null){
            throw new RuntimeException("Không tìm thấy thành tựu!");
        }

        if(achievementDTO.getAchievementTitle()==null && achievementDTO.getAchievementTitle().isEmpty()){
            throw new RuntimeException("Tên thành tựu không được trống!");
        }

        achievement.setTitle(achievementDTO.getAchievementTitle());
        achievement.setDescription(achievementDTO.getAchievementDescription());
        achievement.setRequiredChallenge(achievementDTO.getRequiredChallenge());
        achievement.setRequiredTask(achievementDTO.getRequiredTask());
        achievement.setChallengeBonus(achievementDTO.getBonusChallenge());
        achievement.setTaskBonus(achievementDTO.getBonusTask());
        achievement.setIcon(achievementDTO.getIcon());
        achievement.setCoinBonus(achievementDTO.getCoinBonus());
        achievement.setColor(achievementDTO.getColor());

        this.achievementRepository.save(achievement);
    }

    @Transactional
    public void hideAchievement(Long achievementId) {
        Achievement achievement = this.achievementRepository.getAchievementById(achievementId);
        if(achievement == null){
            throw new RuntimeException("Không tìm thấy thành tựu!");
        }
        achievement.setAvailable(!achievement.isAvailable());
        this.achievementRepository.save(achievement);
    }

    @Transactional
    public void deleteAchievement(Long achievementId) {
        Achievement achievement = this.achievementRepository.getAchievementById(achievementId);
        if(achievement == null){
            throw new RuntimeException("Không tìm thấy thành tựu!");
        }
        List<UserAchievement> userAchievements = this.userAchievementRepository.findAllByAchievementId(achievement.getAchievementId()).orElse(null);

        if(userAchievements != null){
            this.userAchievementRepository.deleteAll(userAchievements);
        }
        this.achievementRepository.delete(achievement);
    }
}
