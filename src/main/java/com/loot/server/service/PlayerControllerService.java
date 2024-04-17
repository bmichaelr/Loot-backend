package com.loot.server.service;

import com.loot.server.domain.entity.dto.PlayerDto;
import org.springframework.http.HttpStatusCode;

import java.util.UUID;

public interface PlayerControllerService {
    HttpStatusCode createNewPlayer(PlayerDto playerDto);
    HttpStatusCode updatePlayerName(UUID clientId, String name);
    HttpStatusCode deletePlayerAccount(UUID uuid);
}
