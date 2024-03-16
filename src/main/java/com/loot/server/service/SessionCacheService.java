package com.loot.server.service;

import java.util.UUID;

public interface SessionCacheService {

    void cacheClientConnection(UUID clientUUID, String gameRoomKey, String simpSessionId);

    void uncacheClientConnection(String simpSessionId);

//    void markClientConnection(String simpSessionId);
//
//    void newConnection(String simpSessionId);
//
//    Boolean clientConnectionExists(String simpSessionId);
}
