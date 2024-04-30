package com.loot.server.logic.impl;

import com.loot.server.domain.cards.Card;
import com.loot.server.logic.IHandOfCards;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class HandOfCards implements IHandOfCards {

    private Integer holdingCard = -1;
    private Integer drawnCard = -1;

    public HandOfCards(Integer cardPower) { holdingCard = cardPower; }

    @Override
    public void playedCard(Integer power) {
        if(Objects.equals(drawnCard, power)) {
            drawnCard = -1;
            return;
        }
        holdingCard = drawnCard;
        drawnCard = -1;
    }

    @Override
    public Integer getCardInHand() {
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
    public Boolean hasTwoCards() { return holdingCard != -1 && drawnCard != -1; }

    @Override
    public String toString() {
        String inHand = "Holding Card: " + ((holdingCard == -1) ? "None" : Card.fromPower(holdingCard).getName());
        String drawn = ", Drawn Card: " + ((drawnCard == -1) ? "None" : Card.fromPower(drawnCard).getName());

        return inHand + drawn;
    }
}
