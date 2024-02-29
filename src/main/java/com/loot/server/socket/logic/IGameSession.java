package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.BaseCard;

public interface IGameSession {

    void playCard(GamePlayer player, BaseCard card);

    void dealInitialCards();

    BaseCard dealCard(GamePlayer player);

    Boolean changePlayerReadyStatus(GamePlayer player);

    void addPlayer(GamePlayer player);
}
