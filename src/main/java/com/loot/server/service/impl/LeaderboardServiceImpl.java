package com.loot.server.service.impl;

import com.loot.server.events.GameWonEvent;
import com.loot.server.domain.entity.LeaderboardEntryEntity;
import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.mappers.Mapper;
import com.loot.server.repositories.LeaderboardRepository;
import com.loot.server.repositories.PlayerRepository;
import com.loot.server.service.LeaderboardService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    private final LeaderboardRepository leaderboardRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Mapper<LeaderboardEntryEntity, LeaderboardEntryDto> mapper;
    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository,
                                  PlayerRepository playerRepository,
                                  SimpMessagingTemplate simpMessagingTemplate,
                                  Mapper<LeaderboardEntryEntity, LeaderboardEntryDto> mapper) {
        this.leaderboardRepository = leaderboardRepository;
        this.playerRepository = playerRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.mapper = mapper;
    }
    @Override
    @EventListener
    public void updateLeaderboard(GameWonEvent gameWonEvent) {
        final UUID playerId = gameWonEvent.getPlayerId();
        Optional<PlayerEntity> optionalPlayer = playerRepository.findPlayerEntityByClientId(playerId);
        if(optionalPlayer.isEmpty()) {
            return; // Anonymous users do not get their wins saved
        }
        PlayerEntity playerEntity = optionalPlayer.get();
        LeaderboardEntryEntity leaderboardEntry;
        Optional<LeaderboardEntryEntity> entry = leaderboardRepository.findEntryByPlayerId(playerId);
        if(entry.isEmpty()) {
            leaderboardEntry = LeaderboardEntryEntity.makeEntryFor(playerEntity.getUuid(), playerEntity.getName());
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
        topEntries.sort((a, b) -> Math.toIntExact(b.getNumberOfWins() - a.getNumberOfWins()));
        final List<LeaderboardEntryDto> truncatedList = topEntries.subList(0, 50);
        simpMessagingTemplate.convertAndSend("/topic/leaderboard", truncatedList);
    }
}
