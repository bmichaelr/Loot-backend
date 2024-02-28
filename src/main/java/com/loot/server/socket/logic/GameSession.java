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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession{

    private List<PlayerDto> players;
    private HashMap<PlayerDto, List<BaseCard>> playedCards;
    private String roomKey;
    private int maxPlayers = 4;
    private int numberOfPlayers = 0;
    private int numberOfReadyPlayers = 0;
    private int turnIndex = 0;
    private int numberOfPlayersLoadedIn = 0;

    private CardStack cardStack;
    //private PlayerHandler playerHandler;

    public GameSession(String roomKey) {
        this.roomKey = roomKey;
        players = new ArrayList<>();
        playedCards = new HashMap<>();
        cardStack = new CardStack();
    }

    @Override
    public void playCard(PlayerDto player, BaseCard card) {

    }

    @Override
    public void dealInitialCards() {

    }

    public BaseCard dealInitialCard(PlayerDto player) {
        numberOfPlayersLoadedIn += 1;
        return cardStack.isDeckEmpty() ? null : cardStack.drawCard();
    }

    @Override
    public BaseCard dealCard(PlayerDto player) {
        return null;
    }

    @Override
    public Boolean changePlayerReadyStatus(PlayerDto player) {
        PlayerDto playerToAlter = players.stream()
                .filter(playerInRoom -> playerInRoom.getId().equals(player.getId()))
                .findFirst()
                .orElse(null);

        if(playerToAlter != null) {
            boolean wasReady = playerToAlter.getReady();
            boolean isReady = player.getReady();

            if(!wasReady && isReady) {
                numberOfReadyPlayers++;
                playerToAlter.setReady(true);
            } else if(wasReady && !isReady) {
                numberOfReadyPlayers--;
                playerToAlter.setReady(false);
            }
        }

        boolean ready =  numberOfReadyPlayers >= maxPlayers;
        if(ready) {
            dealInitialCards();
        }
        return ready;
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
