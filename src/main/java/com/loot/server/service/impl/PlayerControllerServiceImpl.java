package com.loot.server.service.impl;

import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.mappers.Mapper;
import com.loot.server.repositories.PlayerRepository;
import com.loot.server.service.PlayerControllerService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PlayerControllerServiceImpl implements PlayerControllerService {
    private final PlayerRepository playerRepository;
    private final Mapper<PlayerEntity, PlayerDto> mapper;
    public PlayerControllerServiceImpl(PlayerRepository playerRepository, Mapper<PlayerEntity, PlayerDto> mapper) {
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    @Override
    public HttpStatusCode createNewPlayer(PlayerDto playerDto) {
        if(playerDto.missingParameters()) {
            return HttpStatusCode.valueOf(400);
        }
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerByUniqueName(playerDto.getUniqueName());
        if(optionalPlayerEntity.isPresent()) {
            return HttpStatusCode.valueOf(409);
        }
        PlayerEntity playerToCreate = mapper.mapFrom(playerDto);
        playerRepository.save(playerToCreate);
        return HttpStatusCode.valueOf(201);
    }

    @Override
    public HttpStatusCode updatePlayerName(UUID clientId, String name) {
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(clientId);
        if(optionalPlayerEntity.isEmpty()) {
            return HttpStatusCode.valueOf(404);
        }
        PlayerEntity player = optionalPlayerEntity.get();
        player.setName(name);
        playerRepository.save(player);
        return HttpStatusCode.valueOf(200);
    }

    @Override
    public HttpStatusCode deletePlayerAccount(UUID uuid) {
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(uuid);
        if(optionalPlayerEntity.isEmpty()) {
            return HttpStatusCode.valueOf(404);
        }
        PlayerEntity playerToDelete = optionalPlayerEntity.get();
        playerRepository.delete(playerToDelete);
        return HttpStatusCode.valueOf(200);
    }
}
