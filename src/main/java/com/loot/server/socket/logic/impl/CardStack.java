package com.loot.server.socket.logic.impl;

import com.loot.server.socket.logic.ICardStack;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

@NoArgsConstructor
public class CardStack implements ICardStack {

    // Stores all the possible cards
    private final List<Integer> cardPool = List.of(1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8);

    // Acts as the stack of cards for the round
    private Stack<Integer> drawPile;
    @Getter
    private Integer cardKeptOut;

    @Override
    public void shuffle() {
        if(drawPile == null) {
            drawPile = new Stack<>();
        }

        Random rand = new Random();
        List<Integer> cardPoolCopy = new ArrayList<>(cardPool);

        // Remove a random card from the deck
        cardKeptOut = cardPoolCopy.remove(rand.nextInt(cardPoolCopy.size()));

        // Use Fisher-Yates algorithm to shuffle the cards
        for(int i = cardPoolCopy.size() - 1; i > 0; --i){
            var j = rand.nextInt(i + 1);
            var temp = cardPoolCopy.get(i);
            cardPoolCopy.set(i, cardPoolCopy.get(j));
            cardPoolCopy.set(j, temp);
        }

        // Initialize the stack and push the cards onto it
        for(var card : cardPoolCopy) {
            drawPile.push(card);
        }
    }

    @Override
    public Integer drawCard() {
        return drawPile.pop();
    }

    @Override
    public Boolean deckIsEmpty() {
        return drawPile == null || drawPile.isEmpty();
    }
}
