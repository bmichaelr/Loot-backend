package com.loot.server.socket;

import java.util.*;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.request.PlayCardRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.impl.GameControllerServiceImpl.ResponseCode;
import com.loot.server.socket.logic.impl.GameSession;
import com.loot.server.domain.cards.PlayedCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.util.Pair;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@ComponentScan
public class GameController {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private GameControllerService gameService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/createGame")
    public void createGame(@Payload LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode code = gameService.missingRequestParams(request, true);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayerDto().getName(), code);
            return;
        }

        String roomKey = request.getRoomKey();
        String clientName = request.getPlayerDto().getName();
        gameService.createNewGameSession(request, sessionId);
        LobbyResponse response = gameService.getInformationForLobby(roomKey, Boolean.FALSE);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientName, response);
    }

    @MessageMapping("/joinGame")
    public void joinGame(@Payload LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode responseCode = gameService.joinCurrentGameSession(request, sessionId);
        if(responseCode != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayerDto().getName(), responseCode);
            return;
        }

        String roomKey = request.getRoomKey();
        String clientName = request.getPlayerDto().getName();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey, Boolean.FALSE);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientName, lobbyResponse);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/leaveGame")
    public void leaveGame(LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode code = gameService.missingRequestParams(request, true);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayerDto().getName(), code);
            return;
        }

        gameService.removePlayerFromGameSession(request, sessionId);

        String roomKey = request.getRoomKey();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey, Boolean.TRUE);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/ready")
    public void startGame(LobbyRequest request) {
        ResponseCode code = gameService.missingRequestParams(request, true);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayerDto().getName(), code);
            return;
        }

        String roomKey = request.getRoomKey();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey, Boolean.FALSE);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/loadedIntoGame")
    public void loadedIntoGame(LobbyRequest request) {
//        Pair<Boolean, String> validation = validLobbyRequest(request, false);
//        if(!validation.getFirst()) {
//            sendErrorMessage(request, validation.getSecond());
//            return;
//        }
//
//        String roomKey = request.getRoomKey();
//        GamePlayer player = new GamePlayer(request.getPlayerDto());
//        GameSession gameSession = gameSessions.get(roomKey);
//
//        //Card dealtCard = gameSession.dealInitialCard(player);
//        //TurnResponse turnResponse = TurnResponse.builder().card(dealtCard).myTurn(false).build();
//        //messagingTemplate.convertAndSend("/topic/gameplay/"+player.getId()+"/"+roomKey, turnResponse);
    }

//    @MessageMapping("/playCard")
//    public void playCard(PlayCardRequest playCardRequest) {
//        // TODO : add in error checking
//
//        GamePlayer player = new GamePlayer(playCardRequest.getPlayer());
//        GameSession gameSession = gameSessions.get(playCardRequest.getRoomKey());
//        PlayedCard cardPlayed = playCardRequest.getCard();
//        gameSession.playCard(player, cardPlayed);
//    }

    private void sendErrorMessage(String destination, ResponseCode responseCode) {
        String error = switch (responseCode) {
            case MISSING_PLAYER_PARAMS -> "Missing parameters for player object";
            case MISSING_ROOM_KEY -> "Missing room key";
            case LOBBY_FULL -> "Lobby you are trying to join is already full";
            case INVALID_KEY -> "The room key you entered is not valid";
            default -> "Unknown error occurred. Please try again";
        };
        messagingTemplate.convertAndSend(destination, error);
    }

    public static void markedSessionCallback(String clientName, String clientRoomKey) {
        System.out.println(ANSI_RED + "Received a callback for " + clientName + ANSI_RESET);
        if(!gameSessions.containsKey(clientRoomKey)) {
            return;
        }

        GameSession gameSession = gameSessions.get(clientRoomKey);
        for(var player: gameSession.getPlayers()) {
            if(player.getName().equals(clientName)) {
                System.out.println(ANSI_RED + "Removing client (" + clientName + ") from room." + ANSI_RESET);
                gameSession.removePlayer(player);
                updateLobbyOnDisconnect(clientRoomKey, gameSession);
                break;
            }
        }
        validateGameSession(gameSession);
    }

    private static void validateGameSession(GameSession gameSession) {
        if (gameSession.getPlayers().isEmpty()) {
            System.out.println(ANSI_RED + "Game session object with roomKey (" + gameSession.getRoomKey() + ") is empty, removing it..." + ANSI_RESET);
            String roomKey = gameSession.getRoomKey();
            gameSessions.remove(roomKey);
        }

        printDebug();
    }

    private static void updateLobbyOnDisconnect(String lobbyRoomKey, GameSession gameSession) {
        LobbyResponse lobbyResponse = new LobbyResponse(lobbyRoomKey, gameSession.getPlayers(), false);
        //messagingTemplate.convertAndSend("/topic/lobby/" + lobbyRoomKey, lobbyResponse);
    }
}
