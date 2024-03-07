package com.loot.server.socket.logic;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.PlayedCard;

public interface IGameSession {

    /**
     * Main method to play a card. This will handle the logic for the card being played, and do the action
     * accordingly
     * @param playerActing the player who is playing the card
     * @param card the card that has been played
     */
    void playCard(GamePlayer playerActing, PlayedCard card);

    /**
     * Called to start the round. This will instantiate all the data structures and reset any variables that
     * may have been altered from last round
     */
    void startRound();

    /**
     * Called when a player has gotten out. Do cleanup on the player, put any cards they still have into the
     * played cards map and remove them from the players in round list
     * @param playerToRemove from the round
     */
    void removePlayerFromRound(GamePlayer playerToRemove);

    /**
     * Get the player whose next turn it is
     * @return the player to go next
     */
    GamePlayer nextTurn();

    /**
     * This function will be used to sync players when they are loading in to a new round. The game server won't
     * start sending the initial turn information until all the players have loaded in
     * @param player that has successfully loaded in
     * @return true if all players have loaded in
     */
    Boolean loadedIntoGame(GamePlayer player);

    /**
     * Deal the next card to a player, given a player object.
     * @param player the person to deal the card to
     * @return the card that they drew
     */
    Card dealCard(GamePlayer player);

    /**
     * Called to change a player's ready status when inside the lobby.
     * @param player to change the status of
     * @return true if all players are ready, false if not
     */
    Boolean changePlayerReadyStatus(GamePlayer player);

    /**
     * Add a player to the lobby. This call should always be preceded by a call to isLobbyFull
     * @param player to add to the lobby
     * @return true if the addition was successful, false if not
     */
    Boolean addPlayer(GamePlayer player);
}
