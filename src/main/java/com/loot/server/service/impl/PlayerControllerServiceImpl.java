package com.loot.server.service.impl;

import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.exceptions.PlayerControllerException;
import com.loot.server.mappers.Mapper;
import com.loot.server.repositories.PlayerRepository;
import com.loot.server.service.PlayerControllerService;
import org.modelmapper.internal.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    public void createNewPlayer(PlayerDto playerDto) throws PlayerControllerException {
        if(playerDto.missingParameters()) {
            throw PlayerControllerException.badRequest(playerDto.getUuid());
        }
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerByUniqueName(playerDto.getUniqueName());
        if(optionalPlayerEntity.isPresent()) {
            throw PlayerControllerException.nameTaken(playerDto.getUuid());
        }
        PlayerEntity playerToCreate = mapper.mapFrom(playerDto);
        playerRepository.save(playerToCreate);
    }

    @Override
    public PlayerDto updatePlayerName(PlayerDto playerDto) throws PlayerControllerException {
        if(playerDto.missingParameters()) {
            throw PlayerControllerException.badRequest(playerDto.getUuid());
        }
        final UUID clientId = playerDto.getUuid();
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(clientId);
        if(optionalPlayerEntity.isEmpty()) {
            throw PlayerControllerException.notFound(clientId);
        }
        PlayerEntity player = optionalPlayerEntity.get();
        player.setName(playerDto.getName());
        playerRepository.save(player);
        return mapper.mapTo(player);
    }

    @Override
    public void deletePlayerAccount(UUID uuid) throws PlayerControllerException {
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(uuid);
        if(optionalPlayerEntity.isEmpty()) {
            throw PlayerControllerException.notFound(uuid);
        }
        PlayerEntity playerToDelete = optionalPlayerEntity.get();
        playerRepository.delete(playerToDelete);
    }

    @Override
    public PlayerDto getExistingPlayer(UUID uuid) throws PlayerControllerException {
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(uuid);
        if(optionalPlayerEntity.isEmpty()) {
            throw PlayerControllerException.notFound(uuid);
        }
        PlayerEntity playerEntity = optionalPlayerEntity.get();
        return mapper.mapTo(playerEntity);
    }

    @Override
    public List<PlayerDto> getAllPlayers() {
        final List<PlayerDto> players = new ArrayList<>();
        playerRepository.findAll().forEach(playerEntity -> players.add(mapper.mapTo(playerEntity)));
        return players;
    }
}
