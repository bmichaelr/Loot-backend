package com.loot.server.socket.logic.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class HandOfCards {

    private Integer holdingCard;
    private Integer drawnCard;

    public HandOfCards(Integer cardPower) {
        holdingCard = cardPower;
    }

    /**
     * called when a player plays a card, determines if the holding card needs to be updated or left alone
     * e.g. did they play the card they just drew or play the one they already had
     * @param power of the played card
     */
    public void playedCard(Integer power) {
        // If they played the card drawn, do nothing
        if(Objects.equals(drawnCard, power)) return;

        // If they played the card they were holding, update the held card
        holdingCard = drawnCard;
    }

    /**
     * get the card that the person is currently holding
     * @return int of card in hand
     */
    public Integer getCardInHand() {
        if(holdingCard == -1) {
            throw new RuntimeException("This person is no longer in the game!");
        }
        return holdingCard;
    }

    /**
     * method called to draw a card for the player. if they have no card in their hand (e.g. they just discarded) then
     * set the holding card to the drawn card, else we set the drawnCard to the new card
     * @param cardPower of the drawn card
     */
    public void drawCard(Integer cardPower) {
        if(holdingCard == -1) {
            holdingCard = cardPower;
        } else {
            drawnCard = cardPower;
        }
    }

    /**
     * method called to discard a persons hand (whether for net troll, or loot, or otherwise)
     */
    public Integer discardHand() {
        var tmp = holdingCard;
        holdingCard = -1;
        drawnCard = -1;
        return tmp;
    }
}
