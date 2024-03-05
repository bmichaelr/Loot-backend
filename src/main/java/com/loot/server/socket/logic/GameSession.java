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
import org.springframework.data.jpa.repository.query.JSqlParserUtils;

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
    }

    @Override
    public void playCard(GamePlayer playerActing, PlayedCard card) {
        // Get the power of the played card, then remove card from players hand and add it to their played cards
        // This line removes any lingering wishing ring effects
        playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(false);

        var powerOfPlayedCard = card.getPower();
        cardsInHand.get(playerActing).playedCard(powerOfPlayedCard);
        playedCards.get(playerActing).add(Card.cardFromPower(powerOfPlayedCard));

        if(card instanceof TargetedEffectCard effectCard) {
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
                    // Net Troll action here
                    var discardedCard = cardsInHand.get(opponent).discardHand();
                    playedCards.get(opponent).add(Card.cardFromPower(discardedCard));
                    if(cardStack.deckIsEmpty()) {
                        playersInRound.remove(opponent);
                    } else {
                        cardsInHand.get(opponent).drawCard(cardStack.drawCard());
                    }
                }
                case 6 -> {
                    // Do gazebo action here
                    var opponentCard = cardsInHand.get(opponent).discardHand();
                    var playerHand = cardsInHand.get(playerActing).discardHand();
                    cardsInHand.get(opponent).drawCard(playerHand);
                    cardsInHand.get(playerActing).drawCard(opponentCard);
                }
            }
        } else if(card instanceof GuessingCard guessingCard) {
            // Do potted plant action here
            // e.g. get the guessed id, check if they have the guessed card, then send some result
            var opponent = guessingCard.getGuessedOn();
            var guessedCard = guessingCard.getGuessedCard();
            if(cardsInHand.get(opponent).getCardInHand().equals(guessedCard)) {
                // They guessed right
                var cardToDiscard = cardsInHand.get(opponent).discardHand();
                playedCards.get(opponent).add(Card.cardFromPower(cardToDiscard));
                playersInRound.remove(opponent);
            }
            // if they guessed wrong nothing happens
        } else {
            switch (card.getPower()) {
                // Wishing Ring action
                case 4 -> playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(true);
                // Loot action
                case 8 -> playersInRound.remove(playerActing);
            }
        }
    }

    @Override
    public void startRound() {
        cardStack = new CardStack();
        cardStack.shuffle();
        playersInRound = new ArrayList<>(players.size());
        playedCards = new HashMap<>();
        cardsInHand = new HashMap<>();

        players.forEach(gamePlayer -> playedCards.put(gamePlayer, new ArrayList<>()));
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
                numberOfReadyPlayers += 1;
                playerToAlter.setReady(true);
            } else if(wasReady && !isReady) {
                numberOfReadyPlayers -= 1;
                playerToAlter.setReady(false);
            }
        }

        return numberOfReadyPlayers == players.size() && numberOfReadyPlayers >= minPlayers;
    }

    /*
     * increment the number of players by one and set ready to false since they just joined
     */
    @Override
    public void addPlayer(GamePlayer player) {
        if(lobbyIsFull()) {
            return;
        }

        numberOfPlayers += 1;
        player.setReady(false);
        players.add(player);
    }

    /**
     * this function will be used to sync players when they are loading in to a new round
     * @param player that has successfully loaded in
     */
    public Boolean loadedIntoGame(GamePlayer player) {
        if(!players.contains(player)){
            // TODO : these types of safe checking may or may not be needed depending on how well we trust the frontend
            return false;
        }

        players.get(players.indexOf(player)).setLoadedIn(true);
        numberOfPlayersLoadedIn += 1;
        return numberOfPlayersLoadedIn == players.size();
    }

    public boolean lobbyIsFull() {
        return numberOfPlayers >= maxPlayers;
    }
}
