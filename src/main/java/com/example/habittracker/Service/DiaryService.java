package com.example.habittracker.Service;

import com.example.habittracker.DTO.DiaryDTO;
import com.example.habittracker.DTO.TaskDTO;
import com.example.habittracker.Domain.*;
import com.example.habittracker.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final HabitHistoryRepository habitHistoryRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final TodoHistoryRepository todoHistoryRepository;
    private final ImageService imageService;
    private String photoFolder = "diaries";
    private final UserHabitRepository userHabitRepository;
    private final UserDailyRepository userDailyRepository;
    private final TodoRepository todoRepository;
    private final UserService userService;
    private final CoinCalculationService coinCalculationService;
    private final UserChallengeRepository userChallengeRepository;


    public DiaryService(DiaryRepository diaryRepository, HabitHistoryRepository habitHistoryRepository, DailyHistoryRepository dailyHistoryRepository, TodoHistoryRepository todoHistoryRepository, ImageService imageService, UserHabitRepository userHabitRepository, UserDailyRepository userDailyRepository, TodoRepository todoRepository, UserService userService, CoinCalculationService coinCalculationService, UserChallengeRepository userChallengeRepository) {
        this.diaryRepository = diaryRepository;
        this.habitHistoryRepository = habitHistoryRepository;
        this.dailyHistoryRepository = dailyHistoryRepository;
        this.todoHistoryRepository = todoHistoryRepository;
        this.imageService = imageService;
        this.userHabitRepository = userHabitRepository;
        this.userDailyRepository = userDailyRepository;
        this.todoRepository = todoRepository;
        this.userService = userService;
        this.coinCalculationService = coinCalculationService;
        this.userChallengeRepository = userChallengeRepository;
    }
    @Transactional
    public List<Diary> getDiariesByUser(User user) {
        return diaryRepository.findByUser(user);
    }

    @Transactional
    public DiaryDTO getDiaryDTO(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Nhật ký không tìm thấy"));

        List<TaskDTO> completedTasks = new ArrayList<>();

        for (UserHabit habit : diary.getUserHabitList()) {
            completedTasks.add(TaskDTO.builder()
                    .id(habit.getHabit().getHabitId())
                    .title(habit.getHabit().getTitle())
                    .build());
        }

        for (UserDaily daily : diary.getUserDailyList()) {
            completedTasks.add(TaskDTO.builder()
                    .id(daily.getDaily().getDailyId())
                    .title(daily.getDaily().getTitle())
                    .build());
        }

        for (Todo todo : diary.getTodoList()) {
            completedTasks.add(TaskDTO.builder()
                    .id(todo.getTodoId())
                    .title(todo.getTitle())
                    .build());
        }

        return DiaryDTO.builder()
                .diaryId(diary.getDiaryId())
                .date(diary.getDate())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .challengeId(diary.getChallengeId())
                .completedTasks(completedTasks)
                .today(LocalDate.now())
                .build();
    }

    @Transactional
    public String saveDiary(DiaryDTO diaryDTO, MultipartFile image, User user) {
        LocalDate today = LocalDate.now();
        Diary diary = Diary.builder()
                .date(today)
                .content(diaryDTO.getContent())
                .challengeId(diaryDTO.getChallengeId())
                .user(user)
                .userHabitList(new ArrayList<>())
                .userDailyList(new ArrayList<>())
                .todoList(new ArrayList<>())
                .build();

        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveImage(image, photoFolder);
                diary.setImageUrl(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
            }
        }

        // Lấy và gán các task hoàn thành hôm nay
        List<Long> habitIds = habitHistoryRepository.findCompletedHabitIdsByUserAndDate(user, today);
        List<Long> dailyIds = dailyHistoryRepository.findCompletedDailyIdsByUserAndDate(user, today);
        List<Long> todoIds = todoHistoryRepository.findCompletedTodoIdsByUserAndDate(user, today);

        List<UserHabit> completedHabits = habitIds.isEmpty() ? new ArrayList<>() : userHabitRepository.findAllById(habitIds);
        List<UserDaily> completedDailies = dailyIds.isEmpty() ? new ArrayList<>() : userDailyRepository.findAllById(dailyIds);
        List<Todo> completedTodos = todoIds.isEmpty() ? new ArrayList<>() : todoRepository.findAllById(todoIds);

        diary.getUserHabitList().addAll(completedHabits);
        diary.getUserDailyList().addAll(completedDailies);
        diary.getTodoList().addAll(completedTodos);

        Long coinEarned = this.coinCalculationService.calculateDiaryCoins(diary);
        Long actualCoinEarned = this.userService.getCoinComplete(user, coinEarned);
        String message = "+"+actualCoinEarned;

        diaryRepository.save(diary);

        return message;
    }

    @Transactional
    public void updateDiary(DiaryDTO diaryDTO, MultipartFile image) {
        Diary diary = diaryRepository.findById(diaryDTO.getDiaryId())
                .orElseThrow(() -> new RuntimeException("Nhật ký không tìm thấy"));

        diary.setContent(diaryDTO.getContent());
        diary.setChallengeId(diaryDTO.getChallengeId());

        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveImage(image, photoFolder);;
                diary.setImageUrl(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi cập nhật ảnh: " + e.getMessage(), e);
            }
        }

        diary.getUserHabitList().clear();
        diary.getUserDailyList().clear();
        diary.getTodoList().clear();

        LocalDate today = diary.getDate();
        List<Long> habitIds = habitHistoryRepository.findCompletedHabitIdsByUserAndDate(diary.getUser(), today);
        List<Long> dailyIds = dailyHistoryRepository.findCompletedDailyIdsByUserAndDate(diary.getUser(), today);
        List<Long> todoIds = todoHistoryRepository.findCompletedTodoIdsByUserAndDate(diary.getUser(), today);

        List<UserHabit> completedHabits = habitIds.isEmpty() ? new ArrayList<>() : userHabitRepository.findAllById(habitIds);
        List<UserDaily> completedDailies = dailyIds.isEmpty() ? new ArrayList<>() : userDailyRepository.findAllById(dailyIds);
        List<Todo> completedTodos = todoIds.isEmpty() ? new ArrayList<>() : todoRepository.findAllById(todoIds);

        diary.getUserHabitList().addAll(completedHabits);
        diary.getUserDailyList().addAll(completedDailies);
        diary.getTodoList().addAll(completedTodos);

        diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Nhật ký không tìm thấy"));
        diaryRepository.delete(diary);
    }

    @Transactional
    public DiaryDTO updateDiaryTasks(Long diaryId, User user) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Nhật ký không tìm thấy"));
        LocalDate today = LocalDate.now();

        List<Long> habitIds = habitHistoryRepository.findCompletedHabitIdsByUserAndDate(user, today);
        List<Long> dailyIds = dailyHistoryRepository.findCompletedDailyIdsByUserAndDate(user, today);
        List<Long> todoIds = todoHistoryRepository.findCompletedTodoIdsByUserAndDate(user, today);

        List<UserHabit> completedHabits = habitIds.isEmpty() ? new ArrayList<>() : userHabitRepository.findAllById(habitIds);
        List<UserDaily> completedDailies = dailyIds.isEmpty() ? new ArrayList<>() : userDailyRepository.findAllById(dailyIds);
        List<Todo> completedTodos = todoIds.isEmpty() ? new ArrayList<>() : todoRepository.findAllById(todoIds);

        diary.getUserHabitList().clear();
        diary.getUserDailyList().clear();
        diary.getTodoList().clear();

        diary.getUserHabitList().addAll(completedHabits);
        diary.getUserDailyList().addAll(completedDailies);
        diary.getTodoList().addAll(completedTodos);

        diaryRepository.save(diary);
        return getDiaryDTO(diaryId);
    }

    @Transactional
    public List<TaskDTO> getCompletedTasks(User user) {
        LocalDate today = LocalDate.now();

        List<Long> habitIds = habitHistoryRepository.findCompletedHabitIdsByUserAndDate(user, today);
        List<Long> dailyIds = dailyHistoryRepository.findCompletedDailyIdsByUserAndDate(user, today);
        List<Long> todoIds = todoHistoryRepository.findCompletedTodoIdsByUserAndDate(user, today);

        List<TaskDTO> completedTasks = new ArrayList<>();

        if (!habitIds.isEmpty()) {
            List<UserHabit> habits = userHabitRepository.findAllById(habitIds);
            habits.forEach(habit -> completedTasks.add(TaskDTO.builder()
                    .id(habit.getHabit().getHabitId())
                    .title(habit.getHabit().getTitle())
                    .build()));
        }

        if (!dailyIds.isEmpty()) {
            List<UserDaily> dailies = userDailyRepository.findAllById(dailyIds);
            dailies.forEach(daily -> completedTasks.add(TaskDTO.builder()
                    .id(daily.getDaily().getDailyId())
                    .title(daily.getDaily().getTitle())
                    .build()));
        }

        if (!todoIds.isEmpty()) {
            List<Todo> todos = todoRepository.findAllById(todoIds);
            todos.forEach(todo -> completedTasks.add(TaskDTO.builder()
                    .id(todo.getTodoId())
                    .title(todo.getTitle())
                    .build()));
        }

        return completedTasks;
    }

    @Transactional
    public List<DiaryDTO> getDiariesInChallenge(Long userChallengeId) {
        UserChallenge userChallenge = userChallengeRepository.findById(userChallengeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy UserChallenge với ID: " + userChallengeId));

        LocalDate endDate = userChallenge.getEndDate().isAfter(LocalDate.now()) ? LocalDate.now() : userChallenge.getEndDate();

        List<Diary> diaries = diaryRepository.findDiaryInChallenge(
                userChallenge.getUser(),
                userChallenge.getChallenge().getChallengeId(),
                userChallenge.getStartDate(),
                endDate
        );

        return diaries.stream()
                .map(diary -> DiaryDTO.builder()
                        .diaryId(diary.getDiaryId())
                        .date(diary.getDate())
                        .content(diary.getContent())
                        .imageUrl(diary.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
