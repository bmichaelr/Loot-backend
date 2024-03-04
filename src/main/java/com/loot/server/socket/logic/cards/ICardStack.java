package com.loot.server.socket.logic.cards;

public interface ICardStack {

    void shuffle();

    Card drawCard();

    Boolean isDeckEmpty();

}
