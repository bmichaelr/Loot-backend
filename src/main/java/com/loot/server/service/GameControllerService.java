package com.loot.server.service;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.*;
import com.loot.server.logic.impl.GameSession;
import com.loot.server.logic.impl.GameSession.GameAction;
import org.modelmapper.internal.Pair;

import java.util.List;
import java.util.UUID;

public interface GameControllerService {

    /**
     * Create a new game session, or in other words a new game room. The client requesting this will be the
     * first one put into the room, and their session id will be added to the cache.
     * @param request lobby request containing the player information
     * @param sessionId of the client connection
     * @return the created room key
     */
    String createNewGameSession(CreateGameRequest request, String sessionId);

    /**
     * Change the ready status for a certain player. E.g. someone has hit the ready/unready button
     * @param request lobby request containing player information and room key
     */
    void changePlayerReadyStatus(GameInteractionRequest request);

    /**
     * Requested by the client when they wish to leave a lobby.
     * @param gameInteractionRequest containing the player information and the room key
     * @param sessionId of the client leaving, so they can be removed from the cache
     */
    Boolean removePlayerFromGameSession(GameInteractionRequest gameInteractionRequest, String sessionId);

    /**
     * A client wishes to join an already existing lobby.
     * @param request lobby request containing the player information and the room key
     * @param sessionId of the client connection
     */
    void joinCurrentGameSession(JoinGameRequest request, String sessionId);

    /**
     * Check if a game is join able
     * @param roomKey of the game
     * @return true if you can join the room
     */
    Boolean gameAbleToBeJoined(String roomKey);

    /**
     * Check if the room key given by a user even exists
     * @param roomKey to check
     * @return true if the room exists
     */
    Boolean gameExists(String roomKey);

    /**
     * Retrieve the information about a currently instantiated game session
     * @param roomKey that identifies the game session
     * @return lobby response object containing list of players and if they are all ready
     */
    LobbyResponse getInformationForLobby(String roomKey);

    /**
     * Get everyone's first card for the round
     * @param roomKey of the game session
     * @return the list of people and their cards
     */
    StartRoundResponse startRound(String roomKey);

    /**
     * Get the status of the round/game, who won and whether it is over
     * @param roomKey for the game session
     * @return round status response containing winning player and boolean flags
     */
    RoundStatusResponse getRoundStatus(String roomKey);

    /**
     * Used to play a card
     * @param playCardRequest the request containing the player and the card they are playing
     * @return a response indicating what the result was
     */
    PlayedCardResponse playCard(PlayCardRequest playCardRequest);

    /**
     * Return the next player and the card they were dealt
     * @param roomKey of the game session
     * @return the player and the card
     */
    NextTurnResponse getNextTurn(String roomKey);

    /**
     * Sync the player, and return the action that should be taken
     * @param roomKey of game session
     * @param player syncing
     * @return action to take if any
     */
    GameAction syncPlayer(String roomKey, GamePlayer player);

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
     * Return a list of information about all the current servers that are available
     * @return list of server data
     */
    List<ServerData> getListOfServers();

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
}
