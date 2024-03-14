package com.loot.server.service;

import com.loot.server.domain.request.LobbyRequest;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.service.impl.GameControllerServiceImpl.ResponseCode;
import com.loot.server.logic.impl.GameSession;

public interface GameControllerService {

    /**
     * Create a new game session, or in other words a new game room. The client requesting this will be the
     * first one put into the room, and their session id will be added to the cache.
     * @param request lobby request containing the player information
     * @param sessionId of the client connection
     * @return the created room key
     */
    String createNewGameSession(LobbyRequest request, String sessionId);

    /**
     * Change the ready status for a certain player. E.g. someone has hit the ready/unready button
     * @param request lobby request containing player information and room key
     * @return flag indicating if all players are ready or not
     */
    Boolean changePlayerReadyStatus(LobbyRequest request);

    /**
     * Requested by the client when they wish to leave a lobby.
     * @param lobbyRequest containing the player information and the room key
     * @param sessionId of the client leaving, so they can be removed from the cache
     */
    void removePlayerFromGameSession(LobbyRequest lobbyRequest, String sessionId);

    /**
     * A client wishes to join an already existing lobby.
     * @param request lobby request containing the player information and the room key
     * @param sessionId of the client connection
     * @return a response code indicating 1) success or 2) failure, and if so what kind
     */
    ResponseCode joinCurrentGameSession(LobbyRequest request, String sessionId);

    /**
     * Retrieve the information about a currently instantiated game session
     * @param roomKey that identifies the game session
     * @param rFlag flag that indicates if the ready variable should be false or computed
     * @return lobby response object containing list of players and if they are all ready
     */
    LobbyResponse getInformationForLobby(String roomKey, Boolean rFlag);

    /**
     * Update a lobby when a client has disconnected and won't be reconnecting to the server
     * @param gameSession the game session related to the client
     */
    void updateLobbyOnDisconnect(GameSession gameSession);

    /**
     * Forces a game session object to identify it's current state, and if it deems itself no longer needed
     * it will be terminated
     * @param gameSession the game session to do a health check on
     */
    void validateGameSession(GameSession gameSession);

    /**
     * Randomly generate a new room key from a set of allowed characters
     * @return room key
     */
    String generateRoomKey();

    /**
     * Repeatedly makes calls to generateRoomKey until it receives a key that is unique and not currently in use
     * @return the unique room key
     */
    String getRoomKeyForNewGame();

    /**
     * Validation function that takes in a request object and determines if it has all the required parameters needed
     * to continue on with it's intended purpose
     * @param data to validate
     * @param creation flag indicating if this is for creating a game
     * @return code indicating either success, or failure and what type
     */
    ResponseCode missingRequestParams(Object data, Boolean creation);
}
