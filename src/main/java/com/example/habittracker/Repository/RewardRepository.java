package com.example.habittracker.Repository;

import com.example.habittracker.Domain.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

}
