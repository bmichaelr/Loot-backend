package com.loot.server.socket.logic.cards;

import com.loot.server.socket.logic.cards.BaseCard;

public interface ICardStack {

    void shuffle();

    BaseCard drawCard();

    Boolean isDeckEmpty();

}
