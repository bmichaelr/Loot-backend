package com.loot.server.service;

import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.impl.GameControllerServiceImpl.LobbyStatus;

public interface GameControllerService {

    void createNewGameSession(LobbyRequest request);

    void changePlayerReadyStatus(LobbyRequest request);

    LobbyStatus joinCurrentGameSession(LobbyRequest request);

    LobbyResponse getInformationForLobby(String roomKey);
    
    String generateRoomKey();

    String getRoomKeyForNewGame();

    Boolean isValidRoomKey(String roomKey);

}
