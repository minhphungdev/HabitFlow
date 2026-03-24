package com.example.habittracker.Service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SystemRewardProvider {

    public List<Map<String, Object>> getSystemRewards() {
        List<Map<String, Object>> systemRewards = new ArrayList<>();

        systemRewards.add(Map.of(
                "id", 1,
                "title", "Bảo vệ chuỗi",
                "description", "Bảo vệ chuỗi của bạn không bị reset khi quên.",
                "icon", "fa-solid fa-shield-halved",
                "cost", RewardService.COST_STREAK_PROTECTION
        ));
        systemRewards.add(Map.of(
                "id", 2,
                "title", "Thêm giới hạn Task",
                "description", "Tăng giới hạn tạo thói quen và việc cần làm thêm 5.",
                "icon", "fa-solid fa-list-check",
                "cost", RewardService.COST_ADD_TASK_LIMIT
        ));
        systemRewards.add(Map.of(
                "id", 3,
                "title", "Thêm giới hạn Challenge",
                "description", "Tăng giới hạn tạo thử thách cá nhân thêm 1.",
                "icon", "fa-solid fa-trophy",
                "cost", RewardService.COST_ADD_CHALLENGE_LIMIT
        ));

        return systemRewards;
    }
}
