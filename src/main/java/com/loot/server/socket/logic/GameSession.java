package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.CardStack;
import com.loot.server.socket.logic.cards.HandOfCards;
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
    private HashMap<GamePlayer, HandOfCards> cardsInHand;
    private HashMap<GamePlayer, List<Card>> playedCards;
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
        // Get the power of the played card, then remove card from players hand and add it to their played cards
        var powerOfPlayedCard = card.getPower();
        cardsInHand.get(playerActing).playedCard(powerOfPlayedCard);
        playedCards.get(playerActing).add(Card.cardFromPower(powerOfPlayedCard));

        if(card instanceof TargetedEffectCard effectCard) {
            // The opponent
            var opponent = effectCard.getPlayedOn();
            switch(powerOfPlayedCard) {
                case 2 -> {
                    // Do maul rat action here -- send the player who played the card the description of the persons card
                    Card cardInHand = Card.cardFromPower(cardsInHand.get(opponent).getCardInHand());
                    // Show them this card! some sort of response needed here...
                    // TODO ^
                }
                case 3 -> {
                    // Do duck of doom action here --
                    var powerOfOpponentCard = cardsInHand.get(opponent).getCardInHand();
                    if(powerOfOpponentCard > powerOfPlayedCard) {
                        playersInRound.remove(playerActing);
                        playedCards.get(playerActing).add(Card.cardFromPower(powerOfPlayedCard));
                    } else if(powerOfOpponentCard < powerOfPlayedCard) {
                        playersInRound.remove(opponent);
                        playedCards.get(opponent).add(Card.cardFromPower(powerOfOpponentCard));
                    }
                    // On tie, nothing happens
                }
                case 5 -> {
                    // Do net troll action here
                    if(cardStack.deckIsEmpty()) {

                    } else {
                        Integer newCardPower = cardStack.drawCard();
                        // TODO : add in way to swap new card for player hand
                        cardsInHand.get(opponent);
                    }
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
            // TODO check if they guessed right
            if(true) {
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
        players.forEach(gamePlayer -> cardsInHand.put(gamePlayer, new HandOfCards(cardStack.drawCard())));
    }

    @Override
    public Card dealCard(GamePlayer player) {
        if(cardStack.deckIsEmpty()) {
            return null;
        }

        var dealtCard = cardStack.drawCard();
        cardsInHand.get(player).setDrawnCard(dealtCard);
        return Card.cardFromPower(dealtCard);
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
