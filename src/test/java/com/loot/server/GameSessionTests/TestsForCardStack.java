package com.loot.server.GameSessionTests;

import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.CardStack;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestsForCardStack {

    @Test
    public void testThatCardStackShuffleMethodWorks() {
        CardStack cardStack = new CardStack();

        // Deck should be empty to start
        assert cardStack.deckIsEmpty();

        cardStack.shuffle();
        assert !cardStack.deckIsEmpty();
    }

    @Test
    public void testThatThereAre15DrawableCardsBeforeDeckEmpty() {
        CardStack cardStack = new CardStack();
        cardStack.shuffle();
        assert !cardStack.deckIsEmpty();

        var numberOfCards = 0;
        for(int i = 0; i < 15; i++) {
            assert !cardStack.deckIsEmpty();
            var card = cardStack.drawCard();
            numberOfCards += 1;
            System.out.println(Card.fromPower(card));
        }

        assert numberOfCards == 15;
        assert cardStack.deckIsEmpty();
    }

    @Test
    public void testThatTheCardRemovalWorks() {
        CardStack cardStack = new CardStack();
        cardStack.shuffle();

        Map<Integer, Integer> cardCounts = new HashMap<>();
        cardCounts.put(1, 5);
        cardCounts.put(2, 2);
        cardCounts.put(3, 2);
        cardCounts.put(4, 2);
        cardCounts.put(5, 2);
        cardCounts.put(6, 1);
        cardCounts.put(7, 1);
        cardCounts.put(8, 1);

        var cardKeptOut = cardStack.getCardKeptOut();
        cardCounts.put(cardKeptOut, cardCounts.get(cardKeptOut) - 1);

        for(int i = 0; i < 15; i++){
            assert !cardStack.deckIsEmpty();
            var card = cardStack.drawCard();
            cardCounts.put(card, cardCounts.get(card) - 1);
        }

        for(var counts : cardCounts.values()) {
            assert counts.equals(0);
        }
    }

    @Test
    public void testThatReshuffleWorks() {
        var cards = new CardStack();
        assert cards.deckIsEmpty();

        cards.shuffle();
        assert !cards.deckIsEmpty();
        for(int i = 0; i < 15; i++) cards.drawCard();
        assert cards.deckIsEmpty();

        cards.shuffle();
        assert !cards.deckIsEmpty();
    }
}
