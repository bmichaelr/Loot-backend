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
    private int maxPlayers = 4;
    private int numberOfPlayers = 0;
    private int numberOfReadyPlayers = 0;
    private int turnIndex = 0;

    private CardStack cardStack;
    //private PlayerHandler playerHandler;

    public GameSession(String roomKey) {
        this.roomKey = roomKey;
        players = new ArrayList<>();
        cardStack = new CardStack();
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
    public Boolean changePlayerReadyStatus(PlayerDto player) {
        PlayerDto playerDto = players.get(players.indexOf(player));
        if(playerDto != null) {
            boolean wasReady = playerDto.getReady();
            boolean isReady = player.getReady();

            if(!wasReady && isReady) {
                numberOfReadyPlayers++;
                playerDto.setReady(true);
            } else if(wasReady && !isReady) {
                numberOfReadyPlayers--;
                playerDto.setReady(false);
            }
        }

        return numberOfReadyPlayers >= maxPlayers;
    }

    /*
     * increment the number of players by one and set ready to false since they just joined
     */
    @Override
    public void addPlayer(PlayerDto player) {
        numberOfPlayers += 1;
        player.setReady(false);
        players.add(player);
    }

    public boolean lobbyIsFull() {
        return numberOfPlayers >= maxPlayers;
    }
}
