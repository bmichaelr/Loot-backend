package com.loot.server.service;

import com.loot.server.domain.entity.dto.PlayerDto;
import org.springframework.http.HttpStatusCode;

public interface PlayerControllerService {
    HttpStatusCode createNewPlayer(PlayerDto playerDto);
    HttpStatusCode updatePlayerAccount(PlayerDto playerDto);
    HttpStatusCode deletePlayerAccount(PlayerDto playerDto);
}
