package com.loot.server.controllers;

import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.domain.response.ErrorResponse;
import com.loot.server.exceptions.PlayerControllerException;
import com.loot.server.service.PlayerControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@Controller
public class PlayerController {

    @Autowired private PlayerControllerService playerControllerService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/players/create")
    public void createPlayerAccount(PlayerDto playerDto) throws PlayerControllerException {
        playerControllerService.createNewPlayer(playerDto);
        final UUID id = playerDto.getUuid();
        // Not sure what to send, but some sort of
        messagingTemplate.convertAndSend("/topic/player/created/" + id, "Player Created Successfully");
    }
    @MessageMapping("/players/update")
    public void updatePlayerAccount(PlayerDto playerDto) throws PlayerControllerException {
        PlayerDto updatedPlayer = playerControllerService.updatePlayerName(playerDto);
        final UUID id = updatedPlayer.getUuid();
        messagingTemplate.convertAndSend("/topic/player/account/" + id, updatedPlayer);
    }
    @MessageMapping("/players/get")
    public void getPlayerAccountInformation(UUID uuid) throws PlayerControllerException {
        PlayerDto playerDto = playerControllerService.getExistingPlayer(uuid);
        final UUID id = playerDto.getUuid();
        messagingTemplate.convertAndSend("/topic/player/account/" + id, playerDto);
    }
    @MessageMapping("/players/delete")
    public void deletePlayerAccount(UUID uuid) throws PlayerControllerException {
        playerControllerService.deletePlayerAccount(uuid);
    }
    @ExceptionHandler
    void handlePlayerControllerExceptions(PlayerControllerException exception) {
        if(exception.getPlayerID().isEmpty()) {
            return;
        }
        final UUID id = exception.getPlayerID().get();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setDetails(exception.getMessage());
        messagingTemplate.convertAndSend("/topic/error/" + id, errorResponse);
    }
}
