package com.example.habittracker.Service;

import com.example.habittracker.Domain.Reward;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Repository.RewardRepository;
import com.example.habittracker.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RewardService {
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    public static final Long COST_STREAK_PROTECTION = 100L;
    public static final Long COST_ADD_TASK_LIMIT = 200L;
    public static final Long COST_ADD_CHALLENGE_LIMIT = 300L;

    public RewardService(RewardRepository rewardRepository, UserRepository userRepository) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public void save(Reward reward, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng!"));
        if (reward.getTitle().isEmpty()) {
            System.out.println(reward.getTitle());
            throw new RuntimeException("Thêm thất bại tiêu đề đang trống");
        }
        if (reward.getCoinCost() == null||reward.getCoinCost() < 0) {
            throw new RuntimeException("Thêm thất bại coin không hợp lệ!");
        }
        Reward rewardCreate = Reward.builder()
                .title(reward.getTitle())
                .description(reward.getDescription())
                .coinCost(reward.getCoinCost())
                .user(user)
                .build();

        this.rewardRepository.save(rewardCreate);
    }
    @Transactional
    public Reward getRewardById (Long rewardId) {
        return this.rewardRepository.findById(rewardId).get();
    }

    @Transactional
    public void updateReward(Reward reward) {
        Reward updateReward = this.rewardRepository.findById(reward.getRewardId()).get();
        if (updateReward == null) {
            throw new RuntimeException("Không thể cập nhật. Không tìm thấy phần thưởng");
        }
        if(reward.getTitle().isEmpty() || (reward.getCoinCost() == null || reward.getCoinCost() < 0)) {
            throw new RuntimeException("Không thể cập nhật. Yêu cầu không để trống tiêu đề và chi phí");
        }
        updateReward.setTitle(reward.getTitle());
        updateReward.setDescription(reward.getDescription());
        updateReward.setCoinCost(reward.getCoinCost());

        this.rewardRepository.save(updateReward);
    }

    @Transactional
    public void deleteReward(Long rewardId) {
        Reward reward = this.rewardRepository.findById(rewardId).get();
        if(reward == null) {
            throw new RuntimeException("Không tìm thấy phần thưởng cần xóa!");
        }
        this.rewardRepository.delete(reward);
    }

    @Transactional
    public Long exchangeReward(User user, Reward reward) {
        if(user==null || reward==null) {
            throw new RuntimeException("Không tìm thấy người dùng hoặc phần thưởng!");
        }
        if(user.getCoins()<reward.getCoinCost()) {
            throw new RuntimeException("Xu không đủ!");
        }
        Long exchange =reward.getCoinCost();
        user.setCoins(user.getCoins() - reward.getCoinCost());
        this.userRepository.save(user);
        return exchange;
    }

    @Transactional
    public void exchangeSystemReward(User user, int rewardId){
        switch (rewardId){
            case 1:
                if (user.getCoins() < COST_STREAK_PROTECTION) {
                    throw new RuntimeException("Bạn không đủ xu để mua vật phẩm này.");
                }
                user.setCoins(user.getCoins() - COST_STREAK_PROTECTION);
                user.setStreakProtectionCount(user.getStreakProtectionCount() + 1);
                userRepository.save(user);
                break;
            case 2:
                if (user.getCoins() < COST_ADD_TASK_LIMIT) {
                    throw new RuntimeException("Bạn không đủ xu để mua vật phẩm này.");
                }
                user.setCoins(user.getCoins() - COST_ADD_TASK_LIMIT);
                user.setTaskLimit(user.getTaskLimit() + 5);
                userRepository.save(user);
                break;
            case 3:
                if (user.getCoins() < COST_ADD_CHALLENGE_LIMIT) {
                    throw new RuntimeException("Bạn không đủ xu để mua vật phẩm này.");
                }
                user.setCoins(user.getCoins() - COST_ADD_CHALLENGE_LIMIT);
                user.setChallengeLimit(user.getChallengeLimit() + 1);
                userRepository.save(user);
                break;
        }
    }
}
