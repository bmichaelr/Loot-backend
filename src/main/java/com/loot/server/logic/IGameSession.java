package com.loot.server.logic;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.PlayedCard;
import com.loot.server.domain.response.PlayedCardResponse;
import com.loot.server.domain.response.RoundStatusResponse;
import com.loot.server.domain.response.StartRoundResponse;
import com.loot.server.logic.impl.GameSession.GameAction;
import org.modelmapper.internal.Pair;

import java.util.List;

public interface IGameSession {

    /**
     * Main method to play a card. This will handle the logic for the card being played, and do the action
     * accordingly
     * @param playerActing the player who is playing the card
     * @param card the card that has been played
     */
    PlayedCardResponse playCard(GamePlayer playerActing, PlayedCard card);

    /**
     * Called to start the round. This will instantiate all the data structures and reset any variables that
     * may have been altered from last round
     */
    StartRoundResponse startRound();

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
     *
     * @param player that has successfully loaded in
     * @return pair containing status of sync and type
     */
    GameAction syncPlayer(GamePlayer player);

    /**
     * Deal the next card to a player, given a player object.
     * @param player the person to deal the card to
     * @return the card that they drew
     */
    Card dealCard(GamePlayer player);

    /**
     * When we reached the end of a round, this will determine the winner and tell us if the game is over or
     * just the round
     * @return round status response including player who won and booleans to indicate if round or game is over
     */
    RoundStatusResponse determineWinner();

    /**
     * Called to change a player's ready status when inside the lobby.
     * @param player to change the status of
     * @return true if all players are ready, false if not
     */
    Boolean changePlayerReadyStatus(GamePlayer player);

    /**
     * Add a player to the lobby.
     * @param player to add to the lobby
     */
    void addPlayer(GamePlayer player);

    /**
     * Removes a player from the lobby. Normally this should only be called if the user disconnects from
     * the lobby itself, not mid-game.
     * @param player to remove from the lobby
     * @return true if the removal was a success
     */
    Boolean removePlayer(GamePlayer player);
}
