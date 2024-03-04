package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.socket.logic.cards.Card;

public interface IGameSession {

    void playCard(GamePlayer player, Card card);

    void dealInitialCards();

    Card dealCard(GamePlayer player);

    Boolean changePlayerReadyStatus(GamePlayer player);

    void addPlayer(GamePlayer player);
}
