package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.impl.PlayedCard;

public interface IGameSession {

    void playCard(GamePlayer playerActing, PlayedCard card);

    void startRound();

    Card dealCard(GamePlayer player);

    Boolean changePlayerReadyStatus(GamePlayer player);

    void addPlayer(GamePlayer player);
}
