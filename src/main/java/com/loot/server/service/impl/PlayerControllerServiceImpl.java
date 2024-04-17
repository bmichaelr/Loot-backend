package com.loot.server.service.impl;

import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.repositories.PlayerRepository;
import com.loot.server.service.PlayerControllerService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class PlayerControllerServiceImpl implements PlayerControllerService {

    private final PlayerRepository playerRepository;
    private final Mapper<PlayerEntity, PlayerDto>
    @Override
    public HttpStatusCode createNewPlayer(PlayerDto playerDto) {
        return null;
    }

    @Override
    public HttpStatusCode updatePlayerAccount(PlayerDto playerDto) {
        return null;
    }

    @Override
    public HttpStatusCode deletePlayerAccount(PlayerDto playerDto) {
        return null;
    }
}
