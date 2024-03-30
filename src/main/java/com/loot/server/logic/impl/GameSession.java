package com.loot.server.logic.impl;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.GuessingCard;
import com.loot.server.domain.cards.PlayedCard;
import com.loot.server.domain.cards.TargetedEffectCard;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.logic.IGameSession;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession {

  public static final String ANSI_CYAN  = "\u001B[36m";
  public static final String ANSI_RESET = "\u001B[0m";

  private List<GamePlayer> players;                           // Store the list of all players in the game
  private List<GamePlayer> playersInRound;                    // Use by round basis, all players still in
  private HashMap<GamePlayer, HandOfCards> cardsInHand;       // The cards each player is holding
  private HashMap<GamePlayer, List<Card>> playedCards;        // List of all the played cards
  private Map<GamePlayer, Integer> numberOfWins;

  private String roomKey;                                     // The room key that identifies the game session
  private String name;                                        // The name of the room
  private int maxPlayers = 4;                                 // Maximum players allowed in the lobby
  private final int minPlayers = 2;                           // Minimum amount of players (CANT BE CHANGED)
  private int numberOfPlayers = 0;                            // Keep track of the amount of players
  private int numberOfReadyPlayers = 0;                       // Keep track of the amount of ready players
  private int winsNeeded = 3;
  private int turnIndex = 0;                                  // Keep track of the turn index (who's turn it is)
  private int numberOfPlayersLoadedIn = 0;                    // This is used for synchronization across devices
  private boolean gameIsOver = false;                         // Flag indicating the game is over
  private boolean roundIsOver = false;
  private boolean gameInProgress = false;

  private CardStack cardStack;                                // The stack of card used in the round

  public GameSession(String roomKey, String name) {
    this.roomKey = roomKey;
    this.name = name;
    players = new ArrayList<>();
    numberOfWins = new HashMap<>();
  }

  @Override
  public String playCard(GamePlayer playerActing, PlayedCard card) {
    playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(false);

    // Play the card
    int powerOfPlayedCard = card.getPower();
    cardsInHand.get(playerActing).playedCard(powerOfPlayedCard);
    playedCards.get(playerActing).add(Card.fromPower(powerOfPlayedCard));

    // Get the other card they have
    int powerOfPlayerOtherCard = cardsInHand.get(playerActing).getHoldingCard();

    if (card instanceof TargetedEffectCard effectCard) {
      playTargetedEffectCard(effectCard, powerOfPlayerOtherCard, playerActing);
    } else if (card instanceof GuessingCard guessingCard) {
      playGuessingCard(guessingCard);
    } else {
      switch (card.getPower()) {
        // Wishing Ring action
        case 4 -> {
          playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(true);
        }
        // Loot action
        case 8 -> removePlayerFromRound(playerActing);
      }
    }

    if (playersInRound.size() == 1 || cardStack.deckIsEmpty()) {
      // TODO : do something else here, some sort of response to tell the client side the game is over
      roundIsOver = true;
      determineWinner();
    }

    return String.format("%s played %s", playerActing.getName(), Card.fromPower(powerOfPlayedCard).getName());
  }

  private void determineWinner() {
    GamePlayer winningPlayer;
    if (playersInRound.size() == 1) {
      winningPlayer = playersInRound.get(0);
    } else { // Assuming there is more than one player still in
      var index = 0;
      var maxCard = 0;
      for (int i = 0; i < playersInRound.size(); ++i) {
        var player = playersInRound.get(i);
        var cardPower = cardsInHand.get(player).getHoldingCard();
        if (cardPower > maxCard) {
          maxCard = cardPower;
          index = i;
        }
      }
      winningPlayer = playersInRound.get(index);
    }
    Optional<Integer> wins = Optional.ofNullable(numberOfWins.put(winningPlayer, numberOfWins.getOrDefault(winningPlayer, 0) + 1));
    if (wins.isPresent()) {
      int num = wins.get();
      if (num + 1 == winsNeeded) {
        gameIsOver = true;
        System.out.println(ANSI_CYAN + "WINNER OF GAME: " + winningPlayer.getName() + ", wins = " + (num + 1) + ANSI_RESET);
      } else {
        System.out.println(ANSI_CYAN + "WINNER OF ROUND: " + winningPlayer.getName() + ", wins = " + (num + 1) + ANSI_RESET);
      }
    } else {
      System.out.println(ANSI_CYAN + "WINNER OF ROUND: " + winningPlayer.getName() + ", wins = 1" + ANSI_RESET);
    }
  }

  private void playTargetedEffectCard(TargetedEffectCard card, int playersCard, GamePlayer playerActing) {
    var opponent = card.getPlayedOn();
    var playedCard = card.getPower();

    switch (playedCard) {
      case 2 -> { // Maul rat action here
        Card cardInHand = Card.fromPower(cardsInHand.get(opponent).getCardInHand());
        // Show them this card! some sort of response needed here...
        // TODO ^
      }
      case 3 -> { // Duck of doom action here
        int powerOfOpponentCard = cardsInHand.get(opponent).getCardInHand();
        if (powerOfOpponentCard > playersCard) {
          removePlayerFromRound(playerActing);
        } else if (powerOfOpponentCard < playersCard) {
          removePlayerFromRound(opponent);
        }
      }
      case 5 -> { // Net Troll action here
        var discardedCard = cardsInHand.get(opponent).discardHand();
        playedCards.get(opponent).add(Card.fromPower(discardedCard));
        if (cardStack.deckIsEmpty() || discardedCard.equals(8)) {
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
    if (cardsInHand.get(opponent).getCardInHand().equals(guessedCard)) {
      removePlayerFromRound(opponent);
    }
  }

  @Override
  public void startRound() {
    if(!gameInProgress) {
      gameInProgress = true;
    }

    roundIsOver = false;
    cardStack = new CardStack();
    cardStack.shuffle();
    playersInRound = new ArrayList<>(players.size());
    playedCards = new HashMap<>();
    cardsInHand = new HashMap<>();

    // Fill the DS with starting point information
    for (var player : players) {
      playedCards.put(player, new ArrayList<>());
      cardsInHand.put(player, new HandOfCards(cardStack.drawCard()));
      playersInRound.add(player);
    }
  }

  @Override
  synchronized public void removePlayerFromRound(GamePlayer playerToRemove) {
    var index = playersInRound.indexOf(playerToRemove);
    if (index < turnIndex) {
      turnIndex -= 1;
    }
    playersInRound.get(index).setIsOut(true);
    playersInRound.remove(playerToRemove);
    if (cardsInHand.get(playerToRemove).getHoldingCard() != -1) {
      var card = cardsInHand.get(playerToRemove).discardHand();
      playedCards.get(playerToRemove).add(Card.fromPower(card));
    }
  }

  @Override
  public GamePlayer nextTurn() {
    if (turnIndex >= playersInRound.size()) {
      turnIndex = 0;
    }
    var player = playersInRound.get(turnIndex);
    turnIndex += 1;
    return player;
  }

  public GamePlayer nextPlayersTurn() {
    return playersInRound.get(turnIndex);
  }

  @Override
  public Card dealCard(GamePlayer player) {
    if (cardStack.deckIsEmpty()) {
      return null;
    }

    var dealtCard = cardStack.drawCard();
    cardsInHand.get(player).drawCard(dealtCard);
    return Card.fromPower(dealtCard);
  }

  @Override
  synchronized public Boolean changePlayerReadyStatus(GamePlayer player) {
    GamePlayer playerToAlter = players.get(players.indexOf(player));

    boolean wasReady = playerToAlter.getReady();
    boolean isReady = player.getReady();

    if (!wasReady && isReady) {
      numberOfReadyPlayers += 1;
      playerToAlter.setReady(true);
    } else if (wasReady && !isReady) {
      numberOfReadyPlayers -= 1;
      playerToAlter.setReady(false);
    }

    return allPlayersReady();
  }

  public boolean allPlayersReady() {
    return numberOfReadyPlayers == players.size() && numberOfReadyPlayers >= minPlayers;
  }

  @Override
  synchronized public void addPlayer(GamePlayer player) {
    numberOfPlayers += 1;
    player.setReady(false);
    players.add(player);
  }

  @Override
  synchronized public void removePlayer(GamePlayer player) {
    boolean success = players.remove(player);

    if(success) {
      numberOfPlayers -= 1;
      numberOfReadyPlayers = 0;
      for (var p : players) {
        p.setReady(false);
      }
    }
  }

  @Override
  synchronized public Boolean loadedIntoGame(GamePlayer player) {
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
    for (var player : players) {
      stringBuilder.append("\tName: ").append(player.getName()).append(", IsSafe: ").append(player.getIsSafe()).append("\n");
    }
    stringBuilder.append("Players In Round:\n");
    for (var player : playersInRound) {
      stringBuilder.append("\tName: ").append(player.getName()).append(", IsSafe: ").append(player.getIsSafe()).append("\n");
    }
    stringBuilder.append("Cards In Hand:\n");
    for (var player : cardsInHand.keySet()) {
      stringBuilder.append("\tPlayer (").append(player.getName()).append("), Cards: InHand = ");
      stringBuilder.append(cardsInHand.get(player).getHoldingCard()).append(", Drawn = ");
      stringBuilder.append(cardsInHand.get(player).getDrawnCard()).append("\n");
    }
    stringBuilder.append("Played Cards:\n");
    for (var player : playedCards.keySet()) {
      stringBuilder.append("\tPlayer (").append(player.getName()).append("), Cards: ");
      for (var card : playedCards.get(player)) {
        stringBuilder.append(card.getName()).append("(").append(card.getPower()).append("), ");
      }
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }
}
