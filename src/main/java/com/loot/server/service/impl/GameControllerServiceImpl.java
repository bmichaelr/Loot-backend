package com.loot.server.service.impl;

import java.util.*;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.GameControllerService;
import com.loot.server.socket.logic.impl.GameSession;
import org.springframework.stereotype.Service;

@Service
public class GameControllerServiceImpl implements GameControllerService {

    private final Set<String> inUseRoomKeys = new HashSet<>();
    private final Map<String, GameSession> gameSessionMap = new HashMap<>();

    public enum LobbyStatus {
        SUCCESS,
        LOBBY_FULL,
        INVALID_KEY
    }

    @Override
    public void createNewGameSession(LobbyRequest request) {
        String roomKey = getRoomKeyForNewGame();
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        gameSessionMap.put(roomKey, new GameSession(roomKey));
        gameSessionMap.get(roomKey).addPlayer(player);
    }

    @Override
    public LobbyStatus joinCurrentGameSession(LobbyRequest request) {
        String roomKey = request.getRoomKey();
        GameSession gameSession;
        if((gameSession = gameSessionMap.get(roomKey)) == null) {
            return LobbyStatus.INVALID_KEY;
        }

        // Get the player and add them to the game session
        GamePlayer player = new GamePlayer(request.getPlayerDto());
        var additionStatus = gameSession.addPlayer(player);
        if(additionStatus.equals(Boolean.FALSE)) {
            return LobbyStatus.LOBBY_FULL;
        }

        return LobbyStatus.SUCCESS;
    }

    @Override
    public void changePlayerReadyStatus(LobbyRequest request) {

    }

    @Override
    public LobbyResponse getInformationForLobby(String roomKey) {
         return null;
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
