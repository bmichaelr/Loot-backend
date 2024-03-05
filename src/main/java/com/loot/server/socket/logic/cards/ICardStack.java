package com.loot.server.socket.logic.cards;

public interface ICardStack {

    void shuffle();

    Integer drawCard();

    Boolean deckIsEmpty();

}
