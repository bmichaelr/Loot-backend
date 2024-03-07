package com.loot.server.service;

public interface GameService {
    
    String generateRoomKey();

    String getRoomKeyForNewGame();

    Boolean isValidRoomKey(String roomKey);

}
