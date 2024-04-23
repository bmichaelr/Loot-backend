package com.loot.server.controllers;

import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.service.ErrorCheckingService;
import com.loot.server.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LeaderboardController {

    @Autowired private ErrorCheckingService errorCheckingService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private LeaderboardService leaderboardService;

    @MessageMapping("/leaderboard/fetchStandings")
    void fetchLeaderboardStandings(GamePlayer player) {
        if(errorCheckingService.requestContainsError(player)) { return; }
        final UUID playerID = player.getId();
        List<LeaderboardEntryDto> leaderboardStandings = leaderboardService.retrieveLeaderboardStandings();
        messagingTemplate.convertAndSend("/topic/leaderboard/" + playerID, leaderboardStandings);
    }

    @MessageMapping("/leaderboard/myStanding")
    void fetchUsersStanding(GamePlayer player) {
        if(errorCheckingService.requestContainsError(player)) { return; }
        final UUID playerID = player.getId();
        Optional<LeaderboardEntryDto> optionalStanding = leaderboardService.retrievePersonalStanding(player);
        if(optionalStanding.isPresent()) {
            messagingTemplate.convertAndSend("/topic/leaderboard/self/" + playerID, optionalStanding.get());
        }
    }
}
