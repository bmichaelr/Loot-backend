package com.loot.server;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.PlayCardRequest;
import com.loot.server.domain.response.ErrorResponse;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.GameStartResponse;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.impl.GameControllerServiceImpl.ResponseCode;
import org.modelmapper.internal.Pair;
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
            sendErrorMessage("/topic/error/" + request.getPlayer().getId(), code);
            return;
        }

        String roomKey = request.getRoomKey();
        gameService.changePlayerReadyStatus(request);
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/game/loadedIn")
    public void loadedIntoGame(LobbyRequest request) {
        ResponseCode code = gameService.missingRequestParams(request, false);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + request.getPlayer().getId(), code);
            return;
        }

        String roomKey = request.getRoomKey();
        var player = request.getPlayer();
        Boolean roundHasBegun = gameService.playerLoadedIn(roomKey, player);
        if (roundHasBegun) {
             var dealtCards = gameService.getFirstCards(roomKey);
             for(Pair<UUID, Card> pair : dealtCards) {
                 messagingTemplate.convertAndSend("/topic/game/dealtCard/" + pair.getLeft(), pair.getRight());
             }

             // Send the message here?
            Pair<GamePlayer, Card> pair = gameService.nextTurn(roomKey);
            GameStartResponse startResponse = GameStartResponse.builder()
                    .message(pair.getLeft().getName() + " is starting the round.")
                    .startingPlayer(pair.getLeft().getId())
                    .build();
            messagingTemplate.convertAndSend("/topic/game/update/" + roomKey, startResponse);
            messagingTemplate.convertAndSend("/topic/game/dealtCard/" + pair.getLeft().getId(), pair.getRight());
        }
    }

    @MessageMapping("/game/playCard")
    public void playCard(PlayCardRequest playCardRequest) {
        ResponseCode code = gameService.missingRequestParams(playCardRequest, false);
        if(code != ResponseCode.SUCCESS) {
            sendErrorMessage("/topic/error/" + playCardRequest.getPlayer().getId(), code);
            return;
        }

        String roomKey = playCardRequest.getRoomKey();
        var response = gameService.playCard(playCardRequest);
        messagingTemplate.convertAndSend("/topic/game/turnStatus/" + roomKey, response);

        // send new ?
        if(!response.getGameOver() && !response.getRoundOver()) {
            Pair<GamePlayer, Card> pair = gameService.nextTurn(roomKey);
            messagingTemplate.convertAndSend("/topic/game/dealtCard/" + pair.getLeft().getId(), pair.getRight());
        }
    }

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
