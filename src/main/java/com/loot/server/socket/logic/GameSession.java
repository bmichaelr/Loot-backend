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

    private CardStack cardStack;

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
        var powerOfPlayerOtherCard = cardsInHand.get(playerActing).getHoldingCard();
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
                    if(powerOfOpponentCard > powerOfPlayerOtherCard) {
                        playersInRound.remove(playerActing);
                        playedCards.get(playerActing).add(Card.cardFromPower(cardsInHand.get(playerActing).discardHand()));
                    } else if(powerOfOpponentCard < powerOfPlayerOtherCard) {
                        playersInRound.remove(opponent);
                        playedCards.get(opponent).add(Card.cardFromPower(cardsInHand.get(opponent).discardHand()));
                    }
                    // On tie, nothing happens
                }
                case 5 -> {
                    // Net Troll action here
                    var discardedCard = cardsInHand.get(opponent).discardHand();
                    playedCards.get(opponent).add(Card.cardFromPower(discardedCard));
                    if(cardStack.deckIsEmpty() || discardedCard.equals(8)) {
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

        if(playersInRound.size() == 1 || cardStack.deckIsEmpty()) {
            // TODO : do something else here, some sort of response to tell the client side the game is over
            gameIsOver = true;
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
    }

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
    public Boolean lobbyIsFull() {
        return numberOfPlayers >= maxPlayers;
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
