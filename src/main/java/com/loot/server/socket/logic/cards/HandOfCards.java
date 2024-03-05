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
        return holdingCard;
    }
}
