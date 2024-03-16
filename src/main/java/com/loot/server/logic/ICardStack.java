package com.loot.server.logic;

public interface ICardStack {

    /**
     * Shuffles the deck of cards. Will wipe the old deck and replace it with a new one
     */
    void shuffle();

    /**
     * Draw a card from the stack. Throws an error if called on an empty stack
     * @return the next card
     */
    Integer drawCard();

    /**
     * Check if the deck of cards is empty
     * @return true if stack is empty
     */
    Boolean deckIsEmpty();

}
