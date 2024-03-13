package com.loot.server.service.impl;

import java.util.*;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.SessionCacheService;
import com.loot.server.socket.logic.impl.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameControllerServiceImpl implements GameControllerService {

    @Autowired
    private SessionCacheService sessionCacheService;

    private final Set<String> inUseRoomKeys = new HashSet<>();
    private final Map<String, GameSession> gameSessionMap = new HashMap<>();

    public enum ResponseCode {
        SUCCESS,
        LOBBY_FULL,
        INVALID_KEY,
        MISSING_ROOM_KEY,
        MISSING_PLAYER_PARAMS
    }

    @Override
    public void createNewGameSession(LobbyRequest request, String sessionId) {
        String roomKey = getRoomKeyForNewGame();
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        gameSessionMap.put(roomKey, new GameSession(roomKey));
        gameSessionMap.get(roomKey).addPlayer(player);

        sessionCacheService.cacheClientConnection(player.getName(), roomKey, sessionId);
    }

    @Override
    public ResponseCode joinCurrentGameSession(LobbyRequest request, String sessionId) {
        ResponseCode responseCode;
        if((responseCode = missingRequestParams(request, false)) != ResponseCode.SUCCESS) {
            return responseCode;
        }

        String roomKey = request.getRoomKey();
        GameSession gameSession = gameSessionMap.get(roomKey);
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        var additionStatus = gameSession.addPlayer(player);
        if(additionStatus.equals(Boolean.FALSE)) {
            return ResponseCode.LOBBY_FULL;
        }

        sessionCacheService.cacheClientConnection(player.getName(), roomKey, sessionId);
        return ResponseCode.SUCCESS;
    }

    @Override
    public void changePlayerReadyStatus(LobbyRequest request) {
        String roomKey = request.getRoomKey();
        GamePlayer player = new GamePlayer(request.getPlayerDto(), request.getPlayerDto().getReady());
        GameSession gameSession = gameSessionMap.get(roomKey);
        gameSession.changePlayerReadyStatus(player);
    }

    @Override
    public void removePlayerFromGameSession(LobbyRequest lobbyRequest, String sessionId) {
        GamePlayer player = new GamePlayer(lobbyRequest.getPlayerDto());
        var gameSession = gameSessionMap.get(lobbyRequest.getRoomKey());
        gameSession.removePlayer(player);
        sessionCacheService.uncacheClientConnection(sessionId);
    }

    @Override
    public LobbyResponse getInformationForLobby(String roomKey, Boolean rFlag) {
        var gameSession = gameSessionMap.get(roomKey);
        var ready = gameSession.getNumberOfReadyPlayers() == gameSession.getNumberOfPlayers();

        return LobbyResponse.builder()
                .roomKey(roomKey)
                .players(gameSession.getPlayers())
                .allReady(rFlag ? Boolean.FALSE : ready)
                .build();
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
    public Boolean isValidRoomKey(String roomKey) {
        return inUseRoomKeys.contains(roomKey);
    }

    @Override
    public ResponseCode missingRequestParams(Object data, Boolean createGame) {
        if(data instanceof LobbyRequest lobbyRequest) {
            if(!createGame && lobbyRequest.getRoomKey() == null) {
                return ResponseCode.MISSING_ROOM_KEY;
            }
            if(lobbyRequest.getPlayerDto() == null || lobbyRequest.getPlayerDto().missingParam()) {
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
