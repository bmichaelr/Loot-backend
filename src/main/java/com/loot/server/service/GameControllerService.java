package com.loot.server.service;

import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.impl.GameControllerServiceImpl.ResponseCode;

public interface GameControllerService {

    void createNewGameSession(LobbyRequest request, String sessionId);

    void changePlayerReadyStatus(LobbyRequest request);

    void removePlayerFromGameSession(LobbyRequest lobbyRequest, String sessionId);

    ResponseCode joinCurrentGameSession(LobbyRequest request, String sessionId);

    LobbyResponse getInformationForLobby(String roomKey, Boolean rFlag);
    
    String generateRoomKey();

    String getRoomKeyForNewGame();

    Boolean isValidRoomKey(String roomKey);

    ResponseCode missingRequestParams(Object data, Boolean creation);
}
