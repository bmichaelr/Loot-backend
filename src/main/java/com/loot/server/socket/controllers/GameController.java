package com.loot.server.socket.controllers;

import java.util.HashMap;
import java.util.Map;

import com.loot.server.socket.domain.Player;
import com.loot.server.socket.logic.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.socket.domain.GameRequestDto;
import com.loot.server.socket.domain.GameStatus;

@Controller
@ComponentScan
public class GameController {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, GameSession> gameSessions = new HashMap<>();

    @MessageMapping("/createGame")
    public void createGame(GameRequestDto request) throws Exception {
        System.out.println(request);

        String roomKey = request.getRoomKey();

        gameSessions.put(roomKey, new GameSession(roomKey));
        GameSession gameSession = gameSessions.get(roomKey);
        gameSession.addPlayer(request.getPlayerDto());

        String body = mapper.writeValueAsString(GameStatus.builder().message("Game created: " + roomKey).build());
        messagingTemplate.convertAndSend("/topic/gameStatus/" + roomKey, body);

        printDebug();
    }

    @MessageMapping("/joinGame")
    public void joinGame(GameRequestDto request) {
        System.out.println(request);

        String roomKey = request.getRoomKey();
        GameSession gameSession = gameSessions.get(roomKey);
        if (gameSession != null) {
            gameSession.addPlayer(request.getPlayerDto());

            StringBuilder builder = new StringBuilder("Players in room: ");
            gameSession.getPlayers().forEach(player -> builder.append(player).append(" "));
            GameStatus status = GameStatus.builder().message(builder.toString()).build();
            messagingTemplate.convertAndSend("/topic/gameStatus/" + roomKey, status);
        } else {
            messagingTemplate.convertAndSend("/topic/error", "Game session not found: " + roomKey);
        }

        printDebug();
    }

    @MessageMapping("/ready")
    public void startGame(GameRequestDto request) throws Exception {
        String roomKey = request.getRoomKey();
        GameSession gameSession = gameSessions.get(roomKey);
        if(gameSession != null){
            boolean everyoneReady = gameSession.readyPlayerUp(request.getPlayerDto());
            String message = everyoneReady ? "Everyone is ready to play! Starting the game now..." : "Player - " + request.getPlayerDto().getName() + " is ready to begin!";
            String json = mapper.writeValueAsString(
                    GameStatus.builder()
                    .message(message)
                    .build()
            );
            messagingTemplate.convertAndSend("/topic/gameStatus/" + roomKey, json);
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
