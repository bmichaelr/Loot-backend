package com.loot.server.logic.impl;

import com.loot.server.logic.IHandOfCards;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class HandOfCards implements IHandOfCards {

    private Integer holdingCard = -1;
    private Integer drawnCard = -1;

    public HandOfCards(Integer cardPower) {
        holdingCard = cardPower;
    }

    @Override
    public void playedCard(Integer power) {
        // If they played the card drawn, do nothing
        if(Objects.equals(drawnCard, power)) {
            drawnCard = -1;
            return;
        }

        // If they played the card they were holding, update the held card
        holdingCard = drawnCard;
        drawnCard = -1;
    }

    @Override
    public Integer getCardInHand() {
        if(holdingCard == -1) {
            throw new RuntimeException("Cannot get card from player, they have no card");
        }

        return holdingCard;
    }

    @Override
    public void drawCard(Integer cardPower) {
        if(holdingCard == -1) {
            holdingCard = cardPower;
        } else {
            drawnCard = cardPower;
        }
    }

    @Override
    public Integer discardHand() {
        var tmp = holdingCard;
        holdingCard = -1;
        drawnCard = -1;
        return tmp;
    }

    @Override
    public Boolean hasTwoCards() {
        return holdingCard != -1 && drawnCard != -1;
    }

    @Override
    public String toString() {
        return "Hand of Cards:\n\tHolding Card: " + holdingCard + "\n\tDrawn Card: " + drawnCard;
    }
}
