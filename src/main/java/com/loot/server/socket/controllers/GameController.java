package com.loot.server.socket.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loot.server.domain.LobbyResponse;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.service.GameService;
import com.loot.server.socket.logic.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.LobbyRequest;
import com.loot.server.domain.GameStatus;

@Controller
@ComponentScan
public class GameController {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, GameSession> gameSessions = new HashMap<>();

    @MessageMapping("/createGame")
    public void createGame(LobbyRequest request) throws Exception {
        System.out.println("Received lobby request in create game: " + request);

        // Should just have a player dto and no key
        String roomKey = gameService.getRoomKeyForNewGame();
        request.getPlayerDto().setReady(false);
        gameSessions.put(roomKey, new GameSession(roomKey));
        gameSessions.get(roomKey).addPlayer(request.getPlayerDto());

        LobbyResponse lobbyResponse = new LobbyResponse(roomKey, List.of(request.getPlayerDto()));

        messagingTemplate.convertAndSend("/topic/matchmaking/" + request.getPlayerDto().getName(), lobbyResponse);
        printDebug();
    }

    @MessageMapping("/joinGame")
    public void joinGame(LobbyRequest request) {
        System.out.println("Received lobby request in join game: " + request);

        String roomKey = request.getRoomKey();
        if(roomKey != null && gameSessions.containsKey(roomKey)){
            GameSession gameSession = gameSessions.get(roomKey);
            request.getPlayerDto().setReady(false);
            gameSession.addPlayer(request.getPlayerDto());

            LobbyResponse lobbyResponse = new LobbyResponse(roomKey, gameSession.getPlayers());
            messagingTemplate.convertAndSend("/topic/matchmaking/" + request.getPlayerDto().getName(), lobbyResponse);
            messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
        } else {

        }
        printDebug();
    }

    @MessageMapping("/ready")
    public void startGame(LobbyRequest request) throws Exception {
        String roomKey = request.getRoomKey();
        GameSession gameSession = gameSessions.get(roomKey);
        if(gameSession != null){
            boolean everyoneReady = gameSession.readyPlayerUp(request.getPlayerDto());
            String message = everyoneReady ? "Everyone is ready to play! Starting the game now..." : "Player - " + request.getPlayerDto().getName() + " is ready to begin!";

            LobbyResponse lobbyResponse = new LobbyResponse(roomKey, gameSession.getPlayers());
            System.out.println("players for ready endpoint: " + gameSession.getPlayers());
            messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
        } else {
            System.out.println("No players! Room key = " + request.getRoomKey());
        }
    }

    public void printDebug() {
        for(GameSession session : gameSessions.values()) {
            if (session.getPlayers() == null) {
                System.out.println("room key(" + session.getRoomKey() + ") has no players...");
                continue;
            }
            System.out.println("game id = " + session.getRoomKey() + ", players = " + session.getPlayers());
        }
    }
}
