package com.loot.server;

import com.loot.server.domain.response.ErrorResponse;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.impl.GameControllerServiceImpl.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Controller
@ComponentScan
public class GameController {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private GameControllerService gameService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/createGame")
    public void createGame(LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode code = gameService.missingRequestParams(request, true);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayer().getId(), code);
            return;
        }

        UUID clientUUID = request.getPlayer().getId();
        String roomKey = gameService.createNewGameSession(request, sessionId);
        LobbyResponse response = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientUUID, response);
    }

    @MessageMapping("/joinGame")
    public void joinGame(@Payload LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode responseCode = gameService.joinCurrentGameSession(request, sessionId);
        if(responseCode != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayer().getId(), responseCode);
            return;
        }

        String roomKey = request.getRoomKey();
        UUID clientUUID = request.getPlayer().getId();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientUUID, lobbyResponse);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/leaveGame")
    public void leaveGame(LobbyRequest request, @Header("simpSessionId") String sessionId) {
        ResponseCode code = gameService.missingRequestParams(request, false);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayer().getId(), code);
            return;
        }

        gameService.removePlayerFromGameSession(request, sessionId);
        String roomKey = request.getRoomKey();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        if(lobbyResponse != null) { // possibly null if the game session object is removed on last player departure
            messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
        }
    }

    @MessageMapping("/ready")
    public void startGame(LobbyRequest request) {
        ResponseCode code = gameService.missingRequestParams(request, false);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayer().getName(), code);
            return;
        }

        String roomKey = request.getRoomKey();
        Boolean ready = gameService.changePlayerReadyStatus(request);
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
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
        ErrorResponse errorResponse = ErrorResponse.builder().details(error).build();
        messagingTemplate.convertAndSend(destination, errorResponse);
    }
}
