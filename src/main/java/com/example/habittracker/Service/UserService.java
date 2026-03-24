package com.example.habittracker.Service;

import com.example.habittracker.DTO.RecentActivityDTO;
import com.example.habittracker.DTO.UserChallengeStats;
import com.example.habittracker.DTO.UserDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private String folder = "user_avatar";
    private final HabitHistoryRepository habitHistoryRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final TodoHistoryRepository todoHistoryRepository;
    private final UserDailyRepository userDailyRepository;
    private final UserHabitRepository userHabitRepository;
    private final TodoRepository todoRepository;
    private Long coinLimit = 500L ;
    private final EmailService emailService;
    private final UserAchievementRepository userAchievementRepository;
    private final UserChallengeRepository userChallengeRepository;

    public UserService(UserRepository userRepository, ImageService imageService, PasswordEncoder passwordEncoder, HabitHistoryRepository habitHistoryRepository, DailyHistoryRepository dailyHistoryRepository, TodoHistoryRepository todoHistoryRepository, UserDailyRepository userDailyRepository, UserHabitRepository userHabitRepository, TodoRepository todoRepository, EmailService emailService, UserAchievementRepository userAchievementRepository, UserChallengeRepository userChallengeRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
        this.habitHistoryRepository = habitHistoryRepository;
        this.dailyHistoryRepository = dailyHistoryRepository;
        this.todoHistoryRepository = todoHistoryRepository;
        this.userDailyRepository = userDailyRepository;
        this.userHabitRepository = userHabitRepository;
        this.todoRepository = todoRepository;
        this.emailService = emailService;
        this.userAchievementRepository = userAchievementRepository;
        this.userChallengeRepository = userChallengeRepository;
    }

    @Transactional
    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("Không tìm thấy Email!"));
    }

    @Transactional
    public User getUserById(Long id) {
        return this.userRepository.findById(id).get();
    }

    @Transactional
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public Page<User> getAllUsersBySearch(String search,Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        } else {
        return this.userRepository.findAll(pageable);
        }
    }

    @Transactional
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Transactional
    public List<UserChallengeStats> getUsersAndCompletedChallenges() {
        return this.userRepository.findAllUsersOrderedByCompletedChallenges();
    }



    @Transactional
    public List<UserDTO> getAllNewUser() {
        return this.userRepository.findAllUserRegisterToday(LocalDate.now()).stream().map(user->{
            Duration durationSinceRegister = Duration.between(user.getCreateAt(), LocalDateTime.now());
            return UserDTO.builder()
                    .username(user.getUserName())
                    .durationRegister(durationSinceRegister.toHours()).build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public Long getCoinComplete(User user, Long coinEarn) {
        if(user.getCoins() >= 0 && coinEarn > 0){
            if(user.getLimitCoinsEarnedPerDay() < coinLimit){
                user.setCoins(user.getCoins()+coinEarn);
                user.setLimitCoinsEarnedPerDay(user.getLimitCoinsEarnedPerDay()+coinEarn);
            }else{
                return 0L;
            }
        }else{
            user.setCoins(user.getCoins()+coinEarn);
            user.setLimitCoinsEarnedPerDay(user.getLimitCoinsEarnedPerDay()+coinEarn);
            this.userRepository.save(user);
            return coinEarn;
        }
        this.userRepository.save(user);
        return coinEarn;
    }

    @Transactional
    public Long getCoinCompleteForCompleteChallenge(User user, Long coinEarn) {
        if(user.getCoins() >= 0 && coinEarn > 0){
            user.setCoins(user.getCoins()+coinEarn);
            user.setLimitCoinsEarnedPerDay(user.getLimitCoinsEarnedPerDay()+coinEarn);
        }else{
            user.setCoins(user.getCoins()+coinEarn);
            user.setLimitCoinsEarnedPerDay(user.getLimitCoinsEarnedPerDay()+coinEarn);
            this.userRepository.save(user);
            return coinEarn;
        }
        this.userRepository.save(user);
        return coinEarn;
    }


    @Transactional
    public void resetLimitCoin() {
        List<User> users = this.userRepository.findAll().stream().map(user -> {
            user.setLimitCoinsEarnedPerDay(0L);
            return user;
        }).collect(Collectors.toList());

        this.userRepository.saveAll(users);

    }

    @Transactional
    public Integer getUserRank(Long userId) {
        List<UserChallengeStats> usersStats = userRepository.findAllUsersOrderedByCompletedChallenges();
        for (int i = 0; i < usersStats.size(); i++) {
            UserChallengeStats stats = usersStats.get(i);
            if (stats.getUser().getUserId().equals(userId)) {
                return i + 1;
            }
        }
        return null;
    }

    @Transactional
    public long getTaskComplete(User user, boolean inChallenge){
        List<Todo> todos;
        List<UserDaily> userDailies;
        List<UserHabit> userHabits;

        if(inChallenge){
            todos = this.todoRepository.findAllByUser(user);
            userDailies = this.userDailyRepository.findByUserId(user.getUserId());
            userHabits = this.userHabitRepository.findHabitsForUser(user.getUserId());
        }else{
            todos = this.todoRepository.findByUser(user);
            userDailies = this.userDailyRepository.findByUserAndNotInChallengeAndUnavailableFalse(user);
            userHabits = this.userHabitRepository.findByUserAndNotInChallengeAndUnavailableFalse(user);
        }


        long totalCompleteTask = 0;
        for (Todo todo : todos) {
            totalCompleteTask += this.todoHistoryRepository.countCompleteTask(todo);
        }

        for (UserDaily userDaily : userDailies) {
            totalCompleteTask += this.dailyHistoryRepository.countCompleteDaily(userDaily);
        }

        for (UserHabit userHabit : userHabits) {
            totalCompleteTask += this.habitHistoryRepository.countCompleteHabit(userHabit);
        }

        return totalCompleteTask;
    }

    @Transactional
    public void updateUser(User userUpdateInfo, MultipartFile image) {
        User user = this.userRepository.findById(userUpdateInfo.getUserId()).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng!"));

        user.setUserName(userUpdateInfo.getUserName());
        user.setAchieveId(userUpdateInfo.getAchieveId());

        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveImage(image, folder);
                user.setAvatar(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
            }
        }

        this.userRepository.save(user);
    }

    @Transactional
    public UserDTO UserChangePassword(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setJustLoginWithGoogle(user.getPassword() != null && passwordEncoder.matches(AuthService.defaultPassAuth,user.getPassword()));
        return userDTO;
    }

    @Transactional
    public void changePassword(UserDTO userDTO) {
        User user = this.userRepository.findById(userDTO.getUserId()).get();

        //lỗi khi người dùng tạo tk bằng gg xog đổi mật khẩu thành công mà không load lại trang thì chỉ hiển thị 2 input
        if(passwordEncoder.matches(AuthService.defaultPassAuth,user.getPassword())){
            if(userDTO.getPassword().equals(userDTO.getConfirmPassword())){
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }else{
                throw new RuntimeException("Mật khẩu và mật khẩu xác nhận không khớp!");
            }
        } else if (userDTO.getOldPassword()!=null && passwordEncoder.matches(userDTO.getOldPassword(),user.getPassword())) {
            if(userDTO.getPassword().equals(userDTO.getConfirmPassword())){
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }else{
                throw new RuntimeException("Mật khẩu và mật khẩu xác nhận không khớp!");
            }
        }else {
            throw new RuntimeException("Mật khẩu cũ không khớp!");
        }
        this.userRepository.save(user);
    }

    @Transactional
    public UserDTO getUserUpdate(Long userId) {
        User user = this.userRepository.findById(userId).get();
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void AdminUpdateUser(UserDTO userDTO) {
        User user = this.userRepository.findById(userDTO.getUserId()).get();
        user.setUserName(userDTO.getUsername());
        user.setRole(userDTO.getRole().equals("ADMIN")?User.Role.ADMIN:User.Role.USER);

        this.userRepository.save(user);
    }

    @Transactional
    public boolean LockUser(Long userId) {
        User user = this.userRepository.findById(userId).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng!"));
        user.setLocked(!user.isLocked());
        userRepository.save(user);
        return user.isLocked();
    }

    @Transactional
    public void checkUserLogin(User user) {
        LocalDate lastLogin = user.getLastLogin().toLocalDate();
        LocalDate now = LocalDate.now();
        List<UserChallenge> userChallenges= this.userChallengeRepository.findUnCompleteChallengeByUsersId(user.getUserId()).orElse(null);
        if(ChronoUnit.DAYS.between(lastLogin, now) >= 2){
            this.emailService.sendReminderLogin(user,userChallenges,ChronoUnit.DAYS.between(lastLogin, now));
        }
    }

    @Transactional
    public List<RecentActivityDTO> getCombinedRecentActivities(int limit) {
        List<RecentActivityDTO> combinedList = new ArrayList<>();
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

        List<User> recentUsers = userRepository.findAll().stream()
                .filter(u ->u.getCreateAt().isAfter(twoDaysAgo))
                .toList();
        for (User user : recentUsers) {
            combinedList.add(RecentActivityDTO.builder()
                    .userName(user.getUserName())
                    .userAvatar(user.getAvatar())
                    .activityType("Đăng Ký Mới")
                    .description("Người dùng '" + user.getUserName() + "' đã tham gia.")
                    .timestamp(user.getCreateAt())
                    .build());
        }

        List<UserAchievement> recentAchievements = userAchievementRepository.findAll().stream()
                .filter(ua -> ua.getEarnedDate() != null && ua.getEarnedDate().isAfter(twoDaysAgo))
                .toList();
        for (UserAchievement ua : recentAchievements) {
            combinedList.add(RecentActivityDTO.builder()
                    .userName(ua.getUser().getUserName())
                    .userAvatar(ua.getUser().getAvatar())
                    .activityType("Thành Tựu Mới")
                    .description("'" + ua.getUser().getUserName() + "' đã đạt được thành tựu '" + ua.getAchievement().getTitle() + "'.")
                    .timestamp(ua.getEarnedDate())
                    .build());
        }

        List<UserChallenge> completedChallenges = userChallengeRepository.findByStatus(UserChallenge.Status.COMPLETE).stream()
                .filter(uc -> uc.getEndDate() != null && uc.getEndDate().isAfter(LocalDate.now().minusDays(2)))
                .toList();
        for(UserChallenge uc : completedChallenges) {
            combinedList.add(RecentActivityDTO.builder()
                    .userName(uc.getUser().getUserName())
                    .userAvatar(uc.getUser().getAvatar())
                    .activityType("Hoàn Thành Thử Thách")
                    .description("Chúc mừng '" + uc.getUser().getUserName() + "' đã hoàn thành thử thách '" + uc.getChallenge().getTitle() + "'.")
                    .timestamp(uc.getEndDate().atStartOfDay())
                    .build());
        }

        return combinedList.stream()
                .sorted(Comparator.comparing(RecentActivityDTO::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
