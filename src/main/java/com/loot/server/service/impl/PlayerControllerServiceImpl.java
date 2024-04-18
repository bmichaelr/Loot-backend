package com.loot.server.service.impl;

import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.PlayerDto;
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
    public HttpStatus createNewPlayer(PlayerDto playerDto) {
        if(playerDto.missingParameters()) {
            return HttpStatus.BAD_REQUEST;
        }
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerByUniqueName(playerDto.getUniqueName());
        if(optionalPlayerEntity.isPresent()) {
            return HttpStatus.CONFLICT;
        }
        PlayerEntity playerToCreate = mapper.mapFrom(playerDto);
        playerRepository.save(playerToCreate);
        return HttpStatus.CREATED;
    }

    @Override
    public HttpStatus updatePlayerName(UUID clientId, String name) {
        if(clientId == null || name == null) {
            return HttpStatus.BAD_REQUEST;
        }
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(clientId);
        if(optionalPlayerEntity.isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }
        PlayerEntity player = optionalPlayerEntity.get();
        player.setName(name);
        playerRepository.save(player);
        return HttpStatus.OK;
    }

    @Override
    public HttpStatus deletePlayerAccount(UUID uuid) {
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(uuid);
        if(optionalPlayerEntity.isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }
        PlayerEntity playerToDelete = optionalPlayerEntity.get();
        playerRepository.delete(playerToDelete);
        return HttpStatus.OK;
    }

    @Override
    public Pair<Optional<PlayerDto>, HttpStatus> getExistingPlayer(UUID uuid) {
        if(uuid == null) {
            return Pair.of(Optional.empty(), HttpStatus.BAD_REQUEST);
        }
        Optional<PlayerEntity> optionalPlayerEntity = playerRepository.findPlayerEntityByClientId(uuid);
        if(optionalPlayerEntity.isEmpty()) {
            return Pair.of(Optional.empty(), HttpStatus.NOT_FOUND);
        }
        PlayerEntity playerEntity = optionalPlayerEntity.get();
        return Pair.of(Optional.of(mapper.mapTo(playerEntity)), HttpStatus.OK);
    }

    @Override
    public List<PlayerDto> getAllPlayers() {
        final List<PlayerDto> players = new ArrayList<>();
        playerRepository.findAll().forEach(playerEntity -> players.add(mapper.mapTo(playerEntity)));
        return players;
    }
}
