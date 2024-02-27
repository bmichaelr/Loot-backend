package com.loot.server.api;

import com.loot.server.domain.GameCreationDto;
import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loot.server.service.GameService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerRepository playerRepository;
    
    @GetMapping(value = "/game/create")
    public ResponseEntity<?> createNewGameRoom() {
        GameCreationDto createdGame = gameService.getRoomKeyForNewGame();
        return new ResponseEntity<GameCreationDto>(createdGame, HttpStatus.OK);
    }

    @GetMapping(value = "/game/validate")
    public ResponseEntity<?> checkIfValidRoomKey(@RequestParam("key") String roomKey) {
        String decodedKey = URLDecoder.decode(roomKey, StandardCharsets.UTF_8);
        if(gameService.isValidRoomKey(decodedKey)){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/players")
    public ResponseEntity<?> createNewUser(@RequestBody PlayerEntity playerEntity){
        playerRepository.save(playerEntity);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/players")
    public ResponseEntity<List<PlayerEntity>> getPlayersInDb() {
        List<PlayerEntity> playerEntities = new ArrayList<>();
        playerRepository.findAll().forEach(playerEntities::add);
        return new ResponseEntity<>(playerEntities, HttpStatus.OK);
    }
}
