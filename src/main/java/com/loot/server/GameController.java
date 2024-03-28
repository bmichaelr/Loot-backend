package com.loot.server;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.GameStartResponse;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.domain.response.ServerData;
import com.loot.server.service.ErrorCheckingService;
import com.loot.server.service.GameControllerService;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@ComponentScan
public class GameController {

    @Autowired
    private GameControllerService gameService;
    @Autowired
    private ErrorCheckingService errorCheckingService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/loadAvailableServers")
    public void getAvailableServers(GamePlayer playerRequesting) {
        if(errorCheckingService.requestContainsError(playerRequesting)) { return; }

        UUID clientId = playerRequesting.getId();
        List<ServerData> response = gameService.getListOfServers();
        messagingTemplate.convertAndSend("/topic/matchmaking/servers/" + clientId, response);
    }

    @MessageMapping("/createGame")
    public void createGame(CreateGameRequest request, @Header("simpSessionId") String sessionId) {
        System.out.println("Create game request: " + request);
        if(errorCheckingService.requestContainsError(request)) { return; }

        UUID clientUUID = request.getPlayer().getId();
        String roomKey = gameService.createNewGameSession(request, sessionId);
        LobbyResponse response = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientUUID, response);
    }

    @MessageMapping("/joinGame")
    public void joinGame(@Payload JoinGameRequest request, @Header("simpSessionId") String sessionId) {
        if(errorCheckingService.requestContainsError(request)) { return; }

        String roomKey = request.getRoomKey();
        UUID clientUUID = request.getPlayer().getId();
        gameService.joinCurrentGameSession(request, sessionId);
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + clientUUID, lobbyResponse);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/leaveGame")
    public void leaveGame(GameInteractionRequest request, @Header("simpSessionId") String sessionId) {
        if(errorCheckingService.requestContainsError(request)) { return; }

        gameService.removePlayerFromGameSession(request, sessionId);
        String roomKey = request.getRoomKey();
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        if(lobbyResponse != null) {
            messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
        }
    }

    @MessageMapping("/ready")
    public void startGame(GameInteractionRequest request) {
        if(errorCheckingService.requestContainsError(request)) { return; }

        String roomKey = request.getRoomKey();
        gameService.changePlayerReadyStatus(request);
        LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
        messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @MessageMapping("/game/loadedIn")
    public void loadedIntoGame(GameInteractionRequest request) {
        if(errorCheckingService.requestContainsError(request)) { return; }

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
        String roomKey = playCardRequest.getRoomKey();
        var response = gameService.playCard(playCardRequest);
        messagingTemplate.convertAndSend("/topic/game/turnStatus/" + roomKey, response);

        // send new ?
        if(!response.getGameOver() && !response.getRoundOver()) {
            Pair<GamePlayer, Card> pair = gameService.nextTurn(roomKey);
            messagingTemplate.convertAndSend("/topic/game/dealtCard/" + pair.getLeft().getId(), pair.getRight());
        }
    }
}
