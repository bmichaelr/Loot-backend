package com.loot.server;

import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.mappers.Mapper;
import com.loot.server.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private Mapper<PlayerEntity, PlayerDto> mapper;

    @PostMapping(value = "/player/create")
    public ResponseEntity<?> createPlayerAccount(@RequestBody PlayerDto playerDto) {
        System.out.println("Received request: " + playerDto);
        PlayerEntity playerEntity = mapper.mapFrom(playerDto);
        playerRepository.save(playerEntity);
        return ResponseEntity.status(201).build();
    }

    @GetMapping(value = "/player/get")
    public ResponseEntity<List<PlayerEntity>> getSavedPlayers() {
        List<PlayerEntity> playersInDb = new ArrayList<>();
        playerRepository.findAll().forEach(playersInDb::add);
        return ResponseEntity.ok(playersInDb);
    }
}
