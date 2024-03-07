package com.loot.server.api;

import com.loot.server.domain.request.PlayCardRequest;
import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {

    @Autowired
    private PlayerRepository playerRepository;

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

    // TEST
    @PostMapping(value = "/playCard")
    public void playCard(@RequestBody PlayCardRequest playCardRequest) {
        System.out.println(playCardRequest);
        System.out.println(playCardRequest.getCard().toString());
    }
}
