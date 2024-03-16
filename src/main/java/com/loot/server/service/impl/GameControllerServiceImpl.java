package com.loot.server.service.impl;

import java.util.*;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.ClientDisconnectionEvent;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.SessionCacheService;
import com.loot.server.logic.impl.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameControllerServiceImpl implements GameControllerService {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Autowired
    private SessionCacheService sessionCacheService;

    @Autowired // not sure about keeping this in here, but for now it stays
    private SimpMessagingTemplate simpMessagingTemplate;

    private final Set<String> inUseRoomKeys = new HashSet<>();
    private final Map<String, GameSession> gameSessionMap = new HashMap<>();

    public enum ResponseCode {
        SUCCESS,
        LOBBY_FULL,
        INVALID_KEY,
        MISSING_ROOM_KEY,
        MISSING_PLAYER_PARAMS
    }

    synchronized private void addToGameSessionMap(String key, GameSession gameSession) {
        gameSessionMap.put(key, gameSession);
    }

    synchronized private GameSession getFromGameSessionMap(String key) {
        return gameSessionMap.get(key);
    }

    synchronized private void removeFromGameSessionMap(String key) {
        gameSessionMap.remove(key);
    }

    @Override
    public String createNewGameSession(LobbyRequest request, String sessionId) {
        String roomKey = getRoomKeyForNewGame();
        var player = request.getPlayer();

        addToGameSessionMap(roomKey, new GameSession(roomKey));
        var gameSession = getFromGameSessionMap(roomKey);
        gameSession.addPlayer(player);

        sessionCacheService.cacheClientConnection(player.getId(), roomKey, sessionId);
        return roomKey;
    }

    @Override
    public ResponseCode joinCurrentGameSession(LobbyRequest request, String sessionId) {
        ResponseCode responseCode;
        if((responseCode = missingRequestParams(request, false)) != ResponseCode.SUCCESS) {
            return responseCode;
        }

        String roomKey = request.getRoomKey();
        var gameSession = getFromGameSessionMap(roomKey);
        var player = request.getPlayer();
        var additionStatus = gameSession.addPlayer(player);
        if(additionStatus.equals(Boolean.FALSE)) {
            return ResponseCode.LOBBY_FULL;
        }

        sessionCacheService.cacheClientConnection(player.getId(), roomKey, sessionId);
        return ResponseCode.SUCCESS;
    }

    @Override
    public Boolean changePlayerReadyStatus(LobbyRequest request) {
        String roomKey = request.getRoomKey();
        var player = request.getPlayer();
        var gameSession = getFromGameSessionMap(roomKey);
        return gameSession.changePlayerReadyStatus(player);
    }

    @Override
    public void removePlayerFromGameSession(LobbyRequest lobbyRequest, String sessionId) {
        var player = lobbyRequest.getPlayer();
        var gameSession = getFromGameSessionMap(lobbyRequest.getRoomKey());
        gameSession.removePlayer(player);
        sessionCacheService.uncacheClientConnection(sessionId);
        validateGameSession(gameSession);
    }

    @Override
    public LobbyResponse getInformationForLobby(String roomKey) {
        var gameSession = getFromGameSessionMap(roomKey);
        if(gameSession == null) {
            System.out.println("Unable to retrieve game session with roomKey: " + roomKey);
            return null;
        }

        return LobbyResponse.builder()
                .roomKey(roomKey)
                .players(gameSession.getPlayers())
                .allReady(gameSession.allPlayersReady())
                .build();
    }

    @EventListener
    public void clientDisconnectedCallback(ClientDisconnectionEvent clientDisconnectionEvent) {
        UUID clientUUID = clientDisconnectionEvent.getClientUUID();
        String clientRoomKey = clientDisconnectionEvent.getGameRoomKey();

        System.out.println(ANSI_RED + "Received a callback for " + clientUUID + ANSI_RESET);
        var gameSession = getFromGameSessionMap(clientRoomKey);
        if(gameSession == null) {
            return;
        }

        for(var player: gameSession.getPlayers()) {
            if(player.getId().equals(clientUUID)) {
                System.out.println(ANSI_RED + "Removing client (" + clientUUID + ") from room (" + clientRoomKey + ")." + ANSI_RESET);
                gameSession.removePlayer(player);
                updateLobbyOnDisconnect(gameSession);
                break;
            }
        }
        validateGameSession(gameSession);
    }

    @Override
    public void updateLobbyOnDisconnect(GameSession gameSession) {
        String roomKey = gameSession.getRoomKey();
        LobbyResponse lobbyResponse = getInformationForLobby(roomKey);
        simpMessagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @Override
    public void validateGameSession(GameSession gameSession) {
        if (gameSession.getPlayers().isEmpty()) {
            System.out.println(ANSI_RED + "Game session object with roomKey (" + gameSession.getRoomKey() + ") is empty, removing it..." + ANSI_RESET);
            String roomKey = gameSession.getRoomKey();
            removeFromGameSessionMap(roomKey);
        }
    }

    @Override
    public String generateRoomKey() {
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        final int keyLength = 8;
        final int length = allowedCharacters.length() - 1;

        StringBuilder key = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i < keyLength; ++i) {
            key.append(allowedCharacters.charAt(rand.nextInt(length)));
        }

        return key.toString();
    }

    @Override
    public String getRoomKeyForNewGame() {
        String roomKey;
        do {
            roomKey = generateRoomKey();
        } while(inUseRoomKeys.contains(roomKey));
        inUseRoomKeys.add(roomKey);
        // For debugging purposes
        printAllRoomKeys();
        return roomKey;
    }

    @Override
    public ResponseCode missingRequestParams(Object data, Boolean createGame) {
        if(data instanceof LobbyRequest lobbyRequest) {
            if(!createGame && lobbyRequest.getRoomKey() == null) {
                return ResponseCode.MISSING_ROOM_KEY;
            }
            if(lobbyRequest.getPlayer() == null || lobbyRequest.getPlayer().missingParam()) {
                return ResponseCode.MISSING_PLAYER_PARAMS;
            }

            if(!createGame) {
                String roomKey = lobbyRequest.getRoomKey();;
                if(gameSessionMap.get(roomKey) == null) {
                    return ResponseCode.INVALID_KEY;
                }
            }
        }
        return ResponseCode.SUCCESS;
    }

    private void printAllRoomKeys() {
        System.out.print("Current room keys: [");
        int index = 0;
        for(String key : inUseRoomKeys) {
            System.out.print(key);
            if(index++ != inUseRoomKeys.size() - 1){
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
    
}
