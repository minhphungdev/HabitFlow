package com.example.habittracker.DTO;

import com.example.habittracker.Domain.User;

public interface UserChallengeStats {
//    Interface-based Projections
    User getUser();
    Long getCompletedChallengesCount();
}
