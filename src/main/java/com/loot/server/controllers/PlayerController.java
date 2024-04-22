package com.loot.server.controllers;

import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.service.PlayerControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PlayerController {
    @Autowired
    private PlayerControllerService playerControllerService;
    @PostMapping(value = "/api/player/create")
    public ResponseEntity<?> createPlayerAccount(@RequestBody PlayerDto playerDto) {
        HttpStatusCode httpStatusCode = playerControllerService.createNewPlayer(playerDto);
        return ResponseEntity.status(httpStatusCode).build();
    }
    @PutMapping(value = "/api/player/update")
    public ResponseEntity<?> getSavedPlayers(@RequestParam("id") UUID clientId, @RequestParam("name") String newName) {
        HttpStatusCode httpStatusCode = playerControllerService.updatePlayerName(clientId, newName);
        return ResponseEntity.status(httpStatusCode).build();
    }
    @DeleteMapping(value = "/api/player/delete")
    public ResponseEntity<?> deleteExistingPlayer(@RequestParam("id") UUID uuid) {
        HttpStatusCode httpStatusCode = playerControllerService.deletePlayerAccount(uuid);
        return ResponseEntity.status(httpStatusCode).build();
    }
    @GetMapping(value = "/api/player/get")
    public ResponseEntity<?> getPlayerInformation(@RequestParam("id") UUID id) {
        var response = playerControllerService.getExistingPlayer(id);
        if(response.getLeft().isEmpty()) {
            return ResponseEntity.status(response.getRight()).build();
        }
        return ResponseEntity.ok(response.getLeft().get());
    }
    @GetMapping(value = "/api/players")
    public ResponseEntity<List<PlayerDto>> getAllPlayers() {
        final List<PlayerDto> players = playerControllerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }
}
