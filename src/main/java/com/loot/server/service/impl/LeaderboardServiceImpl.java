package com.loot.server.service.impl;

import com.loot.server.GameWonEvent;
import com.loot.server.domain.entity.LeaderboardEntryEntity;
import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.mappers.Mapper;
import com.loot.server.repositories.LeaderboardRepository;
import com.loot.server.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private Mapper<LeaderboardEntryEntity, LeaderboardEntryDto> mapper;
    @Override
    @EventListener
    public void updateLeaderboard(GameWonEvent gameWonEvent) {
        final UUID playerId = gameWonEvent.getPlayerId();
        LeaderboardEntryEntity leaderboardEntry;
        Optional<LeaderboardEntryEntity> entry = leaderboardRepository.findEntryByPlayerId(playerId);
        if(entry.isEmpty()) {
            leaderboardEntry = LeaderboardEntryEntity.makeEntryFor(playerId);
        } else {
            leaderboardEntry = entry.get();
            leaderboardEntry.setNumberOfWins(leaderboardEntry.getNumberOfWins() + 1);
        }
        leaderboardRepository.save(leaderboardEntry);
    }
    @Override
    public void pushUpdatesToUsers() {
        List<LeaderboardEntryDto> topEntries = new ArrayList<>();
        leaderboardRepository.findAll().forEach(entry -> topEntries.add(mapper.mapTo(entry)));
        topEntries.sort((a, b) -> a.getNumberOfWins() < b.getNumberOfWins() ? -1 : 1);
        final List<LeaderboardEntryDto> truncatedList = topEntries.subList(0, 50);
        simpMessagingTemplate.convertAndSend("/topic/leaderboard", truncatedList);
    }
}
