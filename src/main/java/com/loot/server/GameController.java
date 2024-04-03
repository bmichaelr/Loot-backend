package com.loot.server;

import com.loot.server.domain.ObjectDecodingTestsHelper;
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
        System.out.println("Received a message in the server method!");

        if(errorCheckingService.requestContainsError(playerRequesting)) { return; }
        UUID clientId = playerRequesting.getId();
        List<ServerData> response = gameService.getListOfServers();
        messagingTemplate.convertAndSend("/topic/matchmaking/servers/" + clientId, response);
    }

    @MessageMapping("/createGame")
    public void createGame(@Payload CreateGameRequest request, @Header("simpSessionId") String sessionId) {
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
        Boolean success = gameService.removePlayerFromGameSession(request, sessionId);
        String roomKey = request.getRoomKey();

        if(success) {
            LobbyResponse lobbyResponse = gameService.getInformationForLobby(roomKey);
            if (lobbyResponse != null) {
                messagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
            }
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
        String roomKey = playCardRequest.getRoomKey();
        PlayedCardResponse response = gameService.playCard(playCardRequest);
        messagingTemplate.convertAndSend("/topic/game/turnStatus/" + roomKey, response);
    }

    // Tests for swift side
    @MessageMapping("/test/decoding")
    public void testLobbyResponseDecoding(ObjectDecodeTestRequest request) throws Exception {
        Object response = switch(request.getType()) {
            case "LOBBY_RESPONSE"   -> ObjectDecodingTestsHelper.mockLobbyResponse();
            case "SERVERS_RESPONSE" -> ObjectDecodingTestsHelper.mockServersResponse();
            case "GAME_PLAYER"      -> ObjectDecodingTestsHelper.mockGamePlayer();
            case "START_ROUND"      -> ObjectDecodingTestsHelper.mockStartRoundResponse();
            case "NEXT_TURN"        -> ObjectDecodingTestsHelper.mockNextTurnResponse();
            case "ROUND_STATUS"     -> ObjectDecodingTestsHelper.mockRoundStatusResponse();
            case "PLAYED_CARD"      -> ObjectDecodingTestsHelper.mockPlayedCardResponse();
            default -> throw new Exception("Unknown type caught in decoding switch!");
        };
        messagingTemplate.convertAndSend("/topic/test/decoding/" + request.getId(), response);
    }
}
