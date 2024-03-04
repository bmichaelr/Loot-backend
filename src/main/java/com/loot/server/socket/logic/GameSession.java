package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.CardStack;
import com.loot.server.socket.logic.cards.impl.PlayedCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession{

    private List<GamePlayer> players;
    private List<GamePlayer> playersInRound;
    private HashMap<Long, List<Integer>> cardsInHand;
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
        cardsInHand = new HashMap<>();
        cardStack = new CardStack();
    }

    @Override
    public void playCard(GamePlayer playerActing, PlayedCard card) {
        // remove the card from the players hand
        cardsInHand.get(playerActing.getId()).remove(card.getPower());
    }

    @Override
    public void dealInitialCards() {

    }

    public Card dealInitialCard(GamePlayer player) {
        numberOfPlayersLoadedIn += 1;
        Card card = cardStack.drawCard();
        cardsInHand.get(player.getId()).add(card.getPower());
        return card;
    }

    @Override
    public Card dealCard(GamePlayer player) {
        if(cardStack.isDeckEmpty()) {
            return null;
        }

        return cardStack.drawCard();
    }

    @Override
    public Boolean changePlayerReadyStatus(GamePlayer player) {
        GamePlayer playerToAlter = players.stream()
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
            players.forEach(p -> cardsInHand.put(p.getId(), new ArrayList<>()));
            playersInRound = new ArrayList<>(players);
            dealInitialCards();
        }
        return ready;
    }

    /*
     * increment the number of players by one and set ready to false since they just joined
     */
    @Override
    public void addPlayer(GamePlayer player) {
        numberOfPlayers += 1;
        player.setReady(false);
        players.add(player);
    }

    public boolean lobbyIsFull() {
        return numberOfPlayers >= maxPlayers;
    }

    public static void main(String[] args) {
        GameSession gameSession = new GameSession("ABC123");
        GamePlayer player1 = new GamePlayer(PlayerDto.builder().id(1L).name("Ben").build());
        GamePlayer player2 = new GamePlayer(PlayerDto.builder().id(2L).name("Josh").build());
        GamePlayer player3 = new GamePlayer(PlayerDto.builder().id(3L).name("Ian").build());
        GamePlayer player4 = new GamePlayer(PlayerDto.builder().id(4L).name("Kenna").build());

        gameSession.addPlayer(player1);
        gameSession.addPlayer(player2);
        gameSession.addPlayer(player3);
        gameSession.addPlayer(player4);

        Card card = gameSession.dealCard(player1);
        System.out.println(card);
    }
}
