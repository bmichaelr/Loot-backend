package com.loot.server.socket.logic.cards;

import com.loot.server.socket.logic.cards.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class CardStack implements ICardStack {

    // Stores all the possible cards
    private final List<Card> cardPool = List.of(
            Card.pottedPlant(),
            Card.pottedPlant(),
            Card.pottedPlant(),
            Card.pottedPlant(),
            Card.pottedPlant(),
            Card.maulRat(),
            Card.maulRat(),
            Card.duckOfDoom(),
            Card.duckOfDoom(),
            Card.wishingRing(),
            Card.wishingRing(),
            Card.netTroll(),
            Card.netTroll(),
            Card.dreadGazebo(),
            Card.turboniumDragon(),
            Card.loot()
    );

    // Acts as the stack of cards for the round
    private Stack<Card> drawPile;

    public CardStack(){
        shuffle();
    }

    @Override
    public void shuffle() {
        Random rand = new Random();
        List<Card> cardPoolCopy = new ArrayList<>(cardPool);

        // Remove a random card from the deck
        cardPoolCopy.remove(rand.nextInt(cardPoolCopy.size()));

        // Use Fisher-Yates algorithm to shuffle the cards
        for(int i = cardPoolCopy.size() - 1; i > 0; --i){
            int j = rand.nextInt(i + 1);
            Card temp = cardPoolCopy.get(i);
            cardPoolCopy.set(i, cardPoolCopy.get(j));
            cardPoolCopy.set(j, temp);
        }

        // Initialize the stack and push the cards onto it
        drawPile = new Stack<>();
        for(Card card : cardPoolCopy) {
            drawPile.push(card);
        }

        System.out.println(drawPile);
    }

    public static void main(String[] args) {
        CardStack stack = new CardStack();
    }

    @Override
    public Card drawCard() {
        return drawPile.pop();
    }

    @Override
    public Boolean isDeckEmpty() {
        return drawPile.isEmpty();
    }
}
