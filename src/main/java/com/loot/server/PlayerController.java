package com.loot.server;

import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.service.PlayerControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping(value = "/api/player/update")
    public ResponseEntity<?> getSavedPlayers(@RequestParam("id") UUID clientId, @RequestParam("name") String newName) {
        HttpStatusCode httpStatusCode = playerControllerService.updatePlayerName(clientId, newName);
        return ResponseEntity.status(httpStatusCode).build();
    }
    @DeleteMapping(value = "/api/player/delete")
    public ResponseEntity<?> deleteExistingPlayer(@RequestParam("id") UUID uuid) {
        HttpStatusCode httpStatusCode = playerControllerService.deletePlayerAccount(uuid);
        return ResponseEntity.status(httpStatusCode).build();
    }
}
