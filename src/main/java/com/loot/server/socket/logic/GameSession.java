package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.CardStack;
import com.loot.server.socket.logic.cards.impl.GuessingCard;
import com.loot.server.socket.logic.cards.impl.PlayedCard;
import com.loot.server.socket.logic.cards.impl.TargetedEffectCard;
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
    private int minPlayers = 2;
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
        // Remove the card from the players hand
        cardsInHand.get(playerActing.getId()).remove(card.getPower());

        if(card instanceof TargetedEffectCard effectCard) {
            Card opponentCard = Card.cardFromPower(cardsInHand.get(effectCard.getPlayedOnId()).get(0));
            switch(effectCard.getPower()) {
                case 2 -> {
                    // Do maul rat action here -- send the player who played the card the description of the persons card
                    Card cardInHand = Card.cardFromPower(cardsInHand.get(effectCard.getPlayedOnId()).get(0));
                    // Show them this card!
                }
                case 3 -> {
                    // Do duck of doom action here --
                    Card playersCard = Card.cardFromPower(cardsInHand.get(playerActing.getId()).get(0));
                    if(opponentCard.getPower() > playersCard.getPower()) {

                    } else if(opponentCard.getPower() < playersCard.getPower()) {

                    } else {
                        // Nothing happens
                    }
                }
                case 5 -> {
                    // Do net troll action here
                }
                case 6 -> {
                    // Do gazebo action here
                }
            }
        } else if(card instanceof GuessingCard guessingCard) {
            // Do potted plant action here
            // e.g. get the guessed id, check if they have the guessed card, then send some result
            Long idOfGuessedPlayer = guessingCard.getGuessedPlayerId();
            int cardGuessed = guessingCard.getGuessedCard();
            List<Integer> guessedPlayersCards = cardsInHand.get(idOfGuessedPlayer);
            if(guessedPlayersCards.contains(cardGuessed)) {
                // They guessed right
            } else {
                // They guessed wrong
            }
        } else {
            switch (card.getPower()) {
                case 4 -> {
                    // do the ring action here
                }
                case 7 -> {
                    // do the dragon action here
                }
                case 8 -> {
                    // do the loot action here
                }
            }
        }
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

        boolean ready =  numberOfReadyPlayers == players.size() && numberOfReadyPlayers >= minPlayers;
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
