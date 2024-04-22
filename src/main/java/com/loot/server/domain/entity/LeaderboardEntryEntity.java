package com.loot.server.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leaderboard")
public class LeaderboardEntryEntity {
    public static LeaderboardEntryEntity makeEntryFor(UUID playerId) {
        return new LeaderboardEntryEntity(playerId, 1L);
    }
    public LeaderboardEntryEntity(UUID uuid, Long wins) {
        this.playerId = uuid;
        this.numberOfWins = wins;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "player")
    private UUID playerId;
    @Column(name = "wins")
    private Long numberOfWins;
}
