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
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession{

    private List<PlayerDto> players;
    private String roomKey;

    private CardStack cardStack;
    private PlayerHandler playerHandler;

    private int maxPlayers = 4;
    private int numberOfPlayers = 0;
    private int numberOfReadyPlayers = 0;

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

    /*
     * increment the number of players by one and set ready to false since they just joined
     */
    @Override
    public void addPlayer(PlayerDto player) {
        ++numberOfPlayers;
        player.setReady(false);
        players.add(player);
        playerHandler.addPlayer(player);
    }

    public boolean lobbyIsReady() {
        return numberOfReadyPlayers == numberOfPlayers;
    }

    public boolean lobbyIsFull() {
        return numberOfPlayers >= maxPlayers;
    }

    public List<PlayerDto> getPlayers() {
        return playerHandler.getPlayersInRoom();
    }
}
