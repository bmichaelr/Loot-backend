package com.loot.server.socket.logic;

import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.BaseCard;
import com.loot.server.socket.logic.cards.CardStack;
import com.loot.server.socket.logic.playerhandler.PlayerHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession{

    private List<String> players;
    private String roomKey;

    private CardStack cardStack;
    private PlayerHandler playerHandler;

    public GameSession(String roomKey) {
        this.roomKey = roomKey;
        players = new ArrayList<>();
        cardStack = new CardStack();
        playerHandler = new PlayerHandler();
    }

    @Override
    public void playCard(PlayerDto player, BaseCard card) {

    }

    @Override
    public void dealInitialCards() {

    }

    @Override
    public BaseCard dealCard(PlayerDto player) {
        return null;
    }

    @Override
    public Boolean readyPlayerUp(PlayerDto player) {
        return playerHandler.readyUp(player);
    }

    @Override
    public void addPlayer(PlayerDto player) {
        players.add(player.getName());
        playerHandler.addPlayer(player);
    }
}
