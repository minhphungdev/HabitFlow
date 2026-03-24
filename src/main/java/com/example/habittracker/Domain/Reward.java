package com.example.habittracker.Domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rewardId;
    private String title;
    private String description;
    private Long coinCost = 0L;

    @ManyToOne()
    @JoinColumn(name="user_id")
    private User user;

}
