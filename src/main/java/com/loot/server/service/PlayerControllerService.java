package com.loot.server.service;

import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.exceptions.PlayerControllerException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerControllerService {
    void createNewPlayer(PlayerDto playerDto) throws PlayerControllerException;
    void deletePlayerAccount(UUID uuid) throws PlayerControllerException;
    PlayerDto updatePlayerName(PlayerDto playerDto) throws PlayerControllerException;
    PlayerDto getExistingPlayer(UUID uuid) throws PlayerControllerException;
    List<PlayerDto> getAllPlayers();
}
