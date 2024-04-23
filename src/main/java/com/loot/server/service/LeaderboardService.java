package com.loot.server.service;

import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.events.GameWonEvent;

import java.util.List;
import java.util.Optional;

public interface LeaderboardService {
    void updateLeaderboard(GameWonEvent gameWonEvent);
    void pushUpdatesToUsers();
    List<LeaderboardEntryDto> retrieveLeaderboardStandings();
    Optional<LeaderboardEntryDto> retrievePersonalStanding(GamePlayer player);
}
