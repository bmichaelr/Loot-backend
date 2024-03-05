package com.loot.server.GameSessionTests;

import com.loot.server.socket.logic.cards.HandOfCards;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class TestsForCardsInHandObject {

    /*
     * called when a player plays a card, determines if the holding card needs to be updated or left alone
     * e.g. did they play the card they just drew or play the one they already had
     * @param power of the played card
     * public void playedCard(Integer power);
     */

    /*
     * get the card that the person is currently holding
     * @return int of card in hand
     * public Integer getCardInHand();
     */

    /*
     * method called to draw a card for the player. if they have no card in their hand (e.g. they just discarded) then
     * set the holding card to the drawn card, else we set the drawnCard to the new card
     * @param cardPower of the drawn card
     * public void drawCard(Integer cardPower);
     */

    /*
     * method called to discard a persons hand (whether for net troll, or loot, or otherwise)
     * public Integer discardHand();
     */

    @Test
    public void testDrawingCardMethod() {
        HandOfCards handOfCards = new HandOfCards(1);

        // Test that the holding card is the init card
        System.out.println(handOfCards);
        var cardInHand = handOfCards.getCardInHand();
        assert cardInHand.equals(1);
        assert handOfCards.getDrawnCard().equals(-1);

        // Test that when a new card is drawn, values are reflected properly
        System.out.println(handOfCards);
        handOfCards.drawCard(4);
        assert handOfCards.getHoldingCard().equals(1) && handOfCards.getDrawnCard().equals(4);

        // Test that when you play a card that you had, the drawn card becomes the card in hand
        System.out.println(handOfCards);
        handOfCards.playedCard(1);
        assert handOfCards.getHoldingCard().equals(4);
        assert handOfCards.getDrawnCard().equals(-1);

        // Test discard hand
        System.out.println(handOfCards);
        var discard = handOfCards.discardHand();
        assert discard.equals(4);
        assert handOfCards.getHoldingCard().equals(-1);
        assert handOfCards.getDrawnCard().equals(-1);

        // Test draw card again
        System.out.println(handOfCards);
        handOfCards.drawCard(8);
        assert handOfCards.getCardInHand().equals(8);
        assert handOfCards.getDrawnCard().equals(-1);

        // Draw another card
        handOfCards.drawCard(3);
        assert handOfCards.getCardInHand().equals(8);
        assert handOfCards.getDrawnCard().equals(3);

        // Play the card that was in the hand
        handOfCards.playedCard(8);
        assert handOfCards.getCardInHand().equals(3);
        assert handOfCards.getDrawnCard().equals(-1);
    }
}
