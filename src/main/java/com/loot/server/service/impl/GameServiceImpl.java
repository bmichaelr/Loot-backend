package com.loot.server.service.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.loot.server.service.GameService;
import com.loot.server.service.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService{

    private final Set<String> inUseRoomKeys = new HashSet<>();

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
