package com.loot.server.socket.logic;

import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.BaseCard;

public interface IGameSession {

    void playCard(PlayerDto player, BaseCard card);

    void dealInitialCards();

    BaseCard dealCard(PlayerDto player);

    Boolean readyPlayerUp(PlayerDto player);

    void addPlayer(PlayerDto player);
}
