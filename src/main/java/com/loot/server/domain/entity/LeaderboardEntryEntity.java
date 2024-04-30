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
    public static LeaderboardEntryEntity makeEntryFor(UUID playerId, String playerName) {
        return new LeaderboardEntryEntity(playerId, playerName, 1L);
    }
    public LeaderboardEntryEntity(UUID uuid, String name, Long wins) {
        this.playerId = uuid;
        this.playerName = name;
        this.numberOfWins = wins;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "player_id")
    private UUID playerId;
    @Column(name = "player_name")
    private String playerName;
    @Column(name = "wins")
    private Long numberOfWins;
}
