package com.loot.server.service;

import com.loot.server.domain.GameCreationDto;

public interface GameService {
    
    String generateRoomKey();

    GameCreationDto getRoomKeyForNewGame();

    Boolean isValidRoomKey(String roomKey);

}
