package com.loot.server.service;

public interface SessionCacheService {

    void cacheClientConnection(String clientName, String gameRoomKey, String simpSessionId);

    void markClientConnection(String simpSessionId);

    void newConnection(String simpSessionId);

    Boolean clientConnectionExists(String simpSessionId);
}
