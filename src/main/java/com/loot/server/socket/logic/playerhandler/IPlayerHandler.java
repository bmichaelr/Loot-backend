package com.loot.server.socket.logic.playerhandler;

import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.Card;

public interface IPlayerHandler {

    void addPlayer(PlayerDto player);

    Boolean hasNextPlayer();

    PlayerDto getNextPlayer();

    Boolean addWinToPlayer(PlayerDto player);

    void addPlayedCard(PlayerDto player, Card card);

    Boolean readyUp(PlayerDto player);

    void removePlayerFromRound(PlayerDto player);

    void startNewRound();
}
