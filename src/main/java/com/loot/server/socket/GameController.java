package com.loot.server.socket;

import java.util.*;

import com.loot.server.domain.entity.ErrorResponse;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.request.PlayCardRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameService;
import com.loot.server.socket.logic.impl.GameSession;
import com.loot.server.domain.cards.PlayedCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.util.Pair;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        Pair<Boolean, String> validation = validLobbyRequest(request, true);
        if(!validation.getFirst()) {
            sendErrorMessage(request, validation.getSecond());
            return;
        }

        // Create the room key, create the game session, add the player
        String roomKey = gameService.getRoomKeyForNewGame();
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        gameSessions.put(roomKey, new GameSession(roomKey));
        gameSessions.get(roomKey).addPlayer(player);

        LobbyResponse lobbyResponse = new LobbyResponse(roomKey, List.of(player), false);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + player.getName(), lobbyResponse);
    }

    @MessageMapping("/joinGame")
    public void joinGame(LobbyRequest request) {
        Pair<Boolean, String> validation = validLobbyRequest(request, false);
        if(!validation.getFirst()) {
            sendErrorMessage(request, validation.getSecond());
            return;
        }

        String roomKey = request.getRoomKey();
        GameSession gameSession;
        if((gameSession = gameSessions.get(roomKey)) == null) {
            sendErrorMessage(request, "Given room key is not valid.");
            return;
        }

        // Get the player and add them to the game session
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        var additionStatus = gameSession.addPlayer(player);
        if(additionStatus.equals(Boolean.FALSE)) {
            sendErrorMessage(request, "The lobby is already at maximum capacity.");
            return;
        }

        LobbyResponse lobbyResponse = new LobbyResponse(roomKey, gameSession.getPlayers(), false);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + request.getPlayerDto().getName(), lobbyResponse);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/leaveGame")
    public void leaveGame(LobbyRequest request) {
        Pair<Boolean, String> validation = validLobbyRequest(request, false);
        if(!validation.getFirst()) {
            sendErrorMessage(request, validation.getSecond());
            return;
        }

        String roomKey = request.getRoomKey();
        GameSession gameSession;
        if((gameSession = gameSessions.get(roomKey)) == null) {
            sendErrorMessage(request, "Given room key is not valid.");
            return;
        }

        // Get the player and add them to the game session
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        gameSession.removePlayer(player);

        LobbyResponse lobbyResponse = new LobbyResponse(roomKey, gameSession.getPlayers(), false);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);

        System.out.println("Player: " + player.getName() + " has left room => " + roomKey);
        System.out.println(gameSession.getPlayers());
    }

    @MessageMapping("/ready")
    public void startGame(LobbyRequest request) {
        Pair<Boolean, String> validation = validLobbyRequest(request, false);
        if(!validation.getFirst()) {
            sendErrorMessage(request, validation.getSecond());
            return;
        }

        // MAJOR ASSUMPTION : the room key will always be present. more of a frontend issue, but we should account
        // for all edge cases in the future
        String roomKey = request.getRoomKey();
        GamePlayer player = new GamePlayer(request.getPlayerDto(), request.getPlayerDto().getReady());
        GameSession gameSession = gameSessions.get(roomKey);

        boolean everyoneReady = gameSession.changePlayerReadyStatus(player);
        LobbyResponse lobbyResponse = new LobbyResponse(roomKey, gameSession.getPlayers(), everyoneReady);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/loadedIntoGame")
    public void loadedIntoGame(LobbyRequest request) {
        Pair<Boolean, String> validation = validLobbyRequest(request, false);
        if(!validation.getFirst()) {
            sendErrorMessage(request, validation.getSecond());
            return;
        }

        String roomKey = request.getRoomKey();
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        GameSession gameSession = gameSessions.get(roomKey);

        //Card dealtCard = gameSession.dealInitialCard(player);
        //TurnResponse turnResponse = TurnResponse.builder().card(dealtCard).myTurn(false).build();
        //messagingTemplate.convertAndSend("/topic/gameplay/"+player.getId()+"/"+roomKey, turnResponse);
    }

    @MessageMapping("/playCard")
    public void playCard(PlayCardRequest playCardRequest) {
        // TODO : add in error checking

        GamePlayer player = new GamePlayer(playCardRequest.getPlayer());
        GameSession gameSession = gameSessions.get(playCardRequest.getRoomKey());
        PlayedCard cardPlayed = playCardRequest.getCard();
        gameSession.playCard(player, cardPlayed);
    }

    // TODO : move helper function to service class
    public void printDebug() {
        for(GameSession session : gameSessions.values()) {
            if (session.getPlayers() == null) {
                System.out.println("room key(" + session.getRoomKey() + ") has no players...");
                continue;
            }
            System.out.println("game id = " + session.getRoomKey() + ", players = " + session.getPlayers());
        }
    }

    // TODO : move helper function to service class
    private Pair<Boolean, String> validLobbyRequest(LobbyRequest request, boolean create) {
        if(request.getRoomKey() == null && !create) {
            return Pair.of(false, "Missing room key!");
        }
        if(request.getPlayerDto() == null) {
            return Pair.of(false, "Missing player information.");
        }
        if(request.getPlayerDto().missingParam()) {
            return Pair.of(false, request.getPlayerDto().getMissingParam());
        }
        return Pair.of(true, "");
    }

    // TODO : move helper function to service class
    private void sendErrorMessage(LobbyRequest request, String error) {
        if(request.getPlayerDto() == null || request.getPlayerDto().getName() == null) {
            return;
        }
        ErrorResponse errorResponse = ErrorResponse.builder().details(error).build();
        messagingTemplate.convertAndSend("/topic/error/" + request.getPlayerDto().getName(), errorResponse);
    }
}
