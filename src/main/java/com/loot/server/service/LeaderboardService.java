package com.loot.server.service;

import com.loot.server.events.GameWonEvent;

public interface LeaderboardService {
    void updateLeaderboard(GameWonEvent gameWonEvent);
    void pushUpdatesToUsers();
}
