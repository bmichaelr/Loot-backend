package com.loot.server.service;

import com.loot.server.domain.entity.dto.PlayerDto;
import org.modelmapper.internal.Pair;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerControllerService {
    HttpStatus createNewPlayer(PlayerDto playerDto);
    HttpStatus updatePlayerName(UUID clientId, String name);
    HttpStatus deletePlayerAccount(UUID uuid);
    Pair<Optional<PlayerDto>, HttpStatus> getExistingPlayer(UUID uuid);
    List<PlayerDto> getAllPlayers();
}
