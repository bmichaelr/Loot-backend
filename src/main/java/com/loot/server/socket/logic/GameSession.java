package com.loot.server.socket.logic;

import com.loot.server.domain.GamePlayer;
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

    private List<GamePlayer> players;                           // Store the list of all players in the game
    private List<GamePlayer> playersInRound;                    // Use by round basis, all players still in
    private HashMap<GamePlayer, HandOfCards> cardsInHand;       // The cards each player is holding
    private HashMap<GamePlayer, List<Card>> playedCards;        // List of all the played cards
    private String roomKey;                                     // The room key that identifies the game session
    private int maxPlayers = 4;                                 // Maximum players allowed in the lobby
    private final int minPlayers = 2;                           // Minimum amount of players (CANT BE CHANGED)
    private int numberOfPlayers = 0;                            // Keep track of the amount of players
    private int numberOfReadyPlayers = 0;                       // Keep track of the amount of ready players
    private int turnIndex = 0;                                  // Keep track of the turn index (who's turn it is)
    private int numberOfPlayersLoadedIn = 0;                    // This is used for synchronization across devices
    private boolean gameIsOver = false;                         // Flag indicating the game is over

    private CardStack cardStack;                                // The stack of card used in the round

    public GameSession(String roomKey) {
        this.roomKey = roomKey;
        players = new ArrayList<>();
    }

    @Override
    public void playCard(GamePlayer playerActing, PlayedCard card) {
        playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(false);

        // Play the card
        int powerOfPlayedCard = card.getPower();
        cardsInHand.get(playerActing).playedCard(powerOfPlayedCard);
        playedCards.get(playerActing).add(Card.fromPower(powerOfPlayedCard));

        // Get the other card they have
        int powerOfPlayerOtherCard = cardsInHand.get(playerActing).getHoldingCard();

        if(card instanceof TargetedEffectCard effectCard) {
            playTargetedEffectCard(effectCard, powerOfPlayerOtherCard, playerActing);
        } else if(card instanceof GuessingCard guessingCard) {
            playGuessingCard(guessingCard);
        } else {
            switch (card.getPower()) {
                // Wishing Ring action
                case 4 -> playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(true);
                // Loot action
                case 8 -> removePlayerFromRound(playerActing);
            }
        }

        if(playersInRound.size() == 1 || cardStack.deckIsEmpty()) {
            // TODO : do something else here, some sort of response to tell the client side the game is over
            gameIsOver = true;
        }
    }

    private void playTargetedEffectCard(TargetedEffectCard card, int playersCard, GamePlayer playerActing) {
        var opponent = card.getPlayedOn();
        var playedCard = card.getPower();

        switch(playedCard) {
            case 2 -> { // Maul rat action here
                Card cardInHand = Card.fromPower(cardsInHand.get(opponent).getCardInHand());
                // Show them this card! some sort of response needed here...
                // TODO ^
            }
            case 3 -> { // Duck of doom action here
                int powerOfOpponentCard = cardsInHand.get(opponent).getCardInHand();
                if(powerOfOpponentCard > playersCard) {
                    removePlayerFromRound(playerActing);
                } else if(powerOfOpponentCard < playersCard) {
                    removePlayerFromRound(opponent);
                }
            }
            case 5 -> { // Net Troll action here
                var discardedCard = cardsInHand.get(opponent).discardHand();
                playedCards.get(opponent).add(Card.fromPower(discardedCard));
                if(cardStack.deckIsEmpty() || discardedCard.equals(8)) {
                    removePlayerFromRound(opponent);
                } else {
                    cardsInHand.get(opponent).drawCard(cardStack.drawCard());
                }
            }
            case 6 -> { // Do gazebo action here
                var opponentCard = cardsInHand.get(opponent).discardHand();
                var playerHand = cardsInHand.get(playerActing).discardHand();
                cardsInHand.get(opponent).drawCard(playerHand);
                cardsInHand.get(playerActing).drawCard(opponentCard);
            }
        }
    }

    private void playGuessingCard(GuessingCard guessingCard) {
        // Potted plant action here
        var opponent = guessingCard.getGuessedOn();
        var guessedCard = guessingCard.getGuessedCard();
        if(cardsInHand.get(opponent).getCardInHand().equals(guessedCard)) {
            removePlayerFromRound(opponent);
        }
    }

    @Override
    public void startRound() {
        gameIsOver = false;
        cardStack = new CardStack();
        cardStack.shuffle();
        playersInRound = new ArrayList<>(players.size());
        playedCards = new HashMap<>();
        cardsInHand = new HashMap<>();

        // Fill the DS with starting point information
        for(var player : players) {
            playedCards.put(player, new ArrayList<>());
            cardsInHand.put(player, new HandOfCards(cardStack.drawCard()));
            playersInRound.add(player);
        }
    }

    @Override
    public void removePlayerFromRound(GamePlayer playerToRemove) {
        var index = playersInRound.indexOf(playerToRemove);
        if(index < turnIndex) {
            turnIndex -= 1;
        }
        playersInRound.remove(playerToRemove);
        if(cardsInHand.get(playerToRemove).getHoldingCard() != -1) {
            var card = cardsInHand.get(playerToRemove).discardHand();
            playedCards.get(playerToRemove).add(Card.fromPower(card));
        }
    }

    @Override
    public GamePlayer nextTurn() {
        if(turnIndex >= playersInRound.size()) {
            turnIndex = 0;
        }
        var player = playersInRound.get(turnIndex);
        turnIndex += 1;
        return player;
    }

    @Override
    public Card dealCard(GamePlayer player) {
        if(cardStack.deckIsEmpty()) {
            return null;
        }

        var dealtCard = cardStack.drawCard();
        cardsInHand.get(player).drawCard(dealtCard);
        return Card.fromPower(dealtCard);
    }

    @Override
    public Boolean changePlayerReadyStatus(GamePlayer player) {
        if(!players.contains(player)) {
            // TODO throw some error here
        }

        GamePlayer playerToAlter = players.get(players.indexOf(player));

        boolean wasReady = playerToAlter.getReady();
        boolean isReady = player.getReady();

        if(!wasReady && isReady) {
            numberOfReadyPlayers += 1;
            playerToAlter.setReady(true);
        } else if(wasReady && !isReady) {
            numberOfReadyPlayers -= 1;
            playerToAlter.setReady(false);
        }

        return numberOfReadyPlayers == players.size() && numberOfReadyPlayers >= minPlayers;
    }

    @Override
    public Boolean addPlayer(GamePlayer player) {
        if(numberOfPlayers >= maxPlayers) {
            return Boolean.FALSE;
        }

        numberOfPlayers += 1;
        player.setReady(false);
        players.add(player);
        return Boolean.TRUE;
    }

    @Override
    public Boolean loadedIntoGame(GamePlayer player) {
        if(!players.contains(player)){
            // TODO : these types of safe checking may or may not be needed depending on how well we trust the frontend
            return false;
        }

        players.get(players.indexOf(player)).setLoadedIn(true);
        numberOfPlayersLoadedIn += 1;
        return numberOfPlayersLoadedIn == players.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Metadata:    RoomKey: ").append(roomKey).append(", MaxPlayers: ").append(maxPlayers);
        stringBuilder.append(", MinPlayers: ").append(minPlayers).append(", NumberOfPlayers: ").append(numberOfPlayers);
        stringBuilder.append(", NumberOfReadyPlayers: ").append(numberOfReadyPlayers).append(", TurnIndex: ").append(turnIndex);
        stringBuilder.append(", NumberOfPlayersLoadedIn: ").append(numberOfPlayersLoadedIn).append(", GameIsOver: ").append(gameIsOver).append("\n");
        stringBuilder.append("Players in Lobby:\n");
        for(var player : players) {
            stringBuilder.append("\tName: ").append(player.getName()).append(", IsSafe: ").append(player.getIsSafe()).append("\n");
        }
        stringBuilder.append("Players In Round:\n");
        for(var player : playersInRound) {
            stringBuilder.append("\tName: ").append(player.getName()).append(", IsSafe: ").append(player.getIsSafe()).append("\n");
        }
        stringBuilder.append("Cards In Hand:\n");
        for(var player : cardsInHand.keySet()) {
            stringBuilder.append("\tPlayer (").append(player.getName()).append("), Cards: InHand = ");
            stringBuilder.append(cardsInHand.get(player).getHoldingCard()).append(", Drawn = ");
            stringBuilder.append(cardsInHand.get(player).getDrawnCard()).append("\n");
        }
        stringBuilder.append("Played Cards:\n");
        for(var player : playedCards.keySet()) {
            stringBuilder.append("\tPlayer (").append(player.getName()).append("), Cards: ");
            for(var card : playedCards.get(player)) stringBuilder.append(card.getName()).append("(").append(card.getPower()).append("), ");
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
