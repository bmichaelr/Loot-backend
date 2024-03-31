package com.loot.server;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.*;
import com.loot.server.logic.impl.GameSession.GameAction;
import com.loot.server.service.ErrorCheckingService;
import com.loot.server.service.GameControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.*;
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
    public void createGame(@Payload CreateGameRequest request, @Header("simpSessionId") String sessionId) {
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
        System.out.println(ANSIConstants.BLUE_FG + "Received valid request to " + ANSIConstants.YELLOW_FG + "\"/joinGame\"" + ANSIConstants.BLUE_FG + " : " + request + ANSIConstants.RESET);

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

    @MessageMapping("/game/sync")
    public void loadedIntoGame(GameInteractionRequest request) {
        if(errorCheckingService.requestContainsError(request)) { return; }
        System.out.println(ANSIConstants.BLUE_FG + "Received valid request to " + ANSIConstants.YELLOW_FG + "\"/game/sync\"" + ANSIConstants.BLUE_FG + " : " + request + ANSIConstants.RESET);

        String roomKey = request.getRoomKey();
        GamePlayer player = request.getPlayer();
        GameAction actionToTake = gameService.syncPlayer(roomKey, player);
        switch(actionToTake) {
            case START_ROUND -> {
                StartRoundResponse startRoundResponse = gameService.startRound(roomKey);
                messagingTemplate.convertAndSend("/topic/game/startRound/" + roomKey, startRoundResponse);
            }
            case NEXT_TURN -> {
                NextTurnResponse nextTurnResponse = gameService.getNextTurn(roomKey);
                messagingTemplate.convertAndSend("/topic/game/nextTurn/" + roomKey, nextTurnResponse);
            }
            case ROUND_END -> {
                RoundStatusResponse roundStatusResponse = gameService.getRoundStatus(roomKey);
                messagingTemplate.convertAndSend("/topic/game/roundStatus/" + roomKey, roundStatusResponse);
            }
        }
    }

    @MessageMapping("/game/playCard")
    public void playCard(PlayCardRequest playCardRequest) {
        System.out.println("playCardRequest = " + playCardRequest);

        String roomKey = playCardRequest.getRoomKey();
        PlayedCardResponse response = gameService.playCard(playCardRequest);
        messagingTemplate.convertAndSend("/topic/game/turnStatus/" + roomKey, response);
    }
}
