package com.loot.server.logic.impl;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.GuessingCard;
import com.loot.server.domain.cards.PlayedCard;
import com.loot.server.domain.cards.TargetedEffectCard;
import com.loot.server.domain.cards.cardresults.*;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.GameSettings;
import com.loot.server.domain.response.PlayedCardResponse;
import com.loot.server.domain.response.RoundStatusResponse;
import com.loot.server.domain.response.StartRoundResponse;
import com.loot.server.logic.IGameSession;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.modelmapper.internal.Pair;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements IGameSession {

  public enum GameState {
    WAITING_TO_START,
    IN_PROGRESS,
    ROUND_OVER,
    GAME_OVER
  }

  public enum GameAction {
    START_ROUND,
    NEXT_TURN,
    ROUND_END,
    NOTHING
  }

  @Getter
  private enum CardEnum {
    INVALID(-1),
    POTTED(1),
    MAUL_RAT(2),
    DUCK_OF_DOOM(3),
    WISHING_RING(4),
    NET_TROLL(5),
    DREAD_GAZEBO(6),
    DRAGON(7),
    LOOT(8);

    private final int value;

    CardEnum(int value) {
      this.value = value;
    }

    public static CardEnum fromValue(int value) {
      for(CardEnum cardEnum : CardEnum.values()) {
        if (cardEnum.getValue() == value) {
          return cardEnum;
        }
      }
      return INVALID;
    }
  }

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
  private final int minPlayers = 2;                           // Minimum amount of players
  private int numberOfPlayers = 0;                            // Keep track of the amount of players
  private int numberOfReadyPlayers = 0;                       // Keep track of the amount of ready players
  private int winsNeeded = 3;
  private int turnIndex = 0;                                  // Keep track of the turn index (who's turn it is)
  private int numberOfPlayersSynced = 0;                    // This is used for synchronization across devices
  private boolean gameIsOver = false;                         // Flag indicating the game is over
  private boolean roundIsOver = false;
  private boolean gameInProgress = false;

  private GameState gameState = GameState.WAITING_TO_START;

  private CardStack cardStack;                                // The stack of card used in the round

  public GameSession(String roomKey, GameSettings settings) {
    this.roomKey = roomKey;
    this.name = settings.getRoomName();
    this.winsNeeded = settings.getNumberOfWinsNeeded();
    this.maxPlayers = settings.getNumberOfPlayers();
    players = new ArrayList<>();
    numberOfWins = new HashMap<>();
  }

  @Override
  public PlayedCardResponse playCard(GamePlayer playerActing, PlayedCard playedCard) {
    int powerOfPlayedCard = playedCard.getPower();
    cardsInHand.get(playerActing).playedCard(powerOfPlayedCard);
    int powerOfPlayerOtherCard = cardsInHand.get(playerActing).getHoldingCard();
    playedCards.get(playerActing).add(Card.fromPower(powerOfPlayedCard));

    BaseCardResult outcome;
    if (playedCard instanceof TargetedEffectCard effectCard) {
      outcome = playTargetedEffectCard(effectCard, powerOfPlayerOtherCard, playerActing);
    } else if (playedCard instanceof GuessingCard guessingCard) {
      outcome = playGuessingCard(guessingCard);
    } else {
      CardEnum card = CardEnum.fromValue(powerOfPlayedCard);
      outcome = new BaseCardResult(playerActing);
      switch (card) {
        case WISHING_RING -> {
          playerActing.setIsSafe(true);
          players.get(players.indexOf(playerActing)).setIsSafe(true);
          playersInRound.get(playersInRound.indexOf(playerActing)).setIsSafe(true);
        }
        case DRAGON ->  {} //do nothing;
        case LOOT -> {
          playerActing.setIsOut(true);
          players.get(players.indexOf(playerActing)).setIsOut(true);
          removePlayerFromRound(playerActing);
        }
        default -> throw new RuntimeException("Unknown card in personal play section!");
      }
    }

    if (playersInRound.size() == 1 || cardStack.deckIsEmpty()) {
      roundIsOver = true;
      gameState = GameState.ROUND_OVER;
    }
    Boolean waitFlag = playedCard.getPower() == 2 || playedCard.getPower() == 3;
      return PlayedCardResponse.builder()
            .cardPlayed(Card.fromPower(playedCard.getPower()))
            .outcome(outcome)
            .playerWhoPlayed(playerActing)
            .waitFlag(waitFlag)
            .build();
  }

  private BaseCardResult playTargetedEffectCard(TargetedEffectCard card, int playersCard, GamePlayer playerActing) {
    GamePlayer playedOn = card.getPlayedOn();
    int playedCardPower = card.getPower();
    CardEnum playedCard = CardEnum.fromValue(playedCardPower);

      return switch (playedCard != null ? playedCard : CardEnum.INVALID) {
      case MAUL_RAT:
        Card opponentsCard = Card.fromPower(cardsInHand.get(playedOn).getCardInHand());
        yield new MaulRatResult(playedOn, opponentsCard);
      case DUCK_OF_DOOM:
        GamePlayer playerToDiscard = null;
        int powerOfOpponentCard = cardsInHand.get(playedOn).getCardInHand();
        if (powerOfOpponentCard > playersCard) {
          playerActing.setIsOut(true);
          players.get(players.indexOf(playerActing)).setIsOut(true);
          removePlayerFromRound(playerActing);
          playerToDiscard = playerActing;
        } else if (powerOfOpponentCard < playersCard) {
          playedOn.setIsOut(true);
          players.get(players.indexOf(playedOn)).setIsOut(true);
          removePlayerFromRound(playedOn);
          playerToDiscard = playedOn;
        }
        yield new DuckResult(playedOn, Card.fromPower(powerOfOpponentCard), Card.fromPower(playersCard), playerToDiscard);
      case NET_TROLL:
        int discardedCard = cardsInHand.get(playedOn).discardHand();
        playedCards.get(playedOn).add(Card.fromPower(discardedCard));
        Card drawnCard = null;
        if (cardStack.deckIsEmpty() || discardedCard == 8) {
          removePlayerFromRound(playedOn);
          playedOn.setIsOut(true);
          players.get(players.indexOf(playedOn)).setIsOut(true);
        } else {
          int newCard = cardStack.drawCard();
          cardsInHand.get(playedOn).drawCard(newCard);
          drawnCard = Card.fromPower(newCard);
        }
        yield new NetTrollResult(playedOn, Card.fromPower(discardedCard), drawnCard);

      case DREAD_GAZEBO:
        int opponentCard = cardsInHand.get(playedOn).discardHand();
        int playerHand = cardsInHand.get(playerActing).discardHand();
        cardsInHand.get(playedOn).drawCard(playerHand);
        cardsInHand.get(playerActing).drawCard(opponentCard);
        yield new GazeboResult(playedOn, Card.fromPower(opponentCard), Card.fromPower(playerHand));

      default:
        yield null;
    };
  }

  private BaseCardResult playGuessingCard(GuessingCard guessingCard) {
    GamePlayer playedOn = guessingCard.getGuessedOn();
    int guessedCard = guessingCard.getGuessedCard();
    boolean successfulGuess = cardsInHand.get(playedOn).getCardInHand().equals(guessedCard);

    if (successfulGuess) {
      playedOn.setIsOut(true);
      removePlayerFromRound(playedOn);
    }

    return new PottedResult(playedOn, Card.fromPower(guessedCard), successfulGuess);
  }

  @Override
  public RoundStatusResponse determineWinner() {
    GamePlayer winningPlayer;
    if (playersInRound.size() == 1) {
      winningPlayer = playersInRound.get(0);
    } else {
      int index = 0;
      int maxCard = 0;
      for (int i = 0; i < playersInRound.size(); ++i) {
        GamePlayer player = playersInRound.get(i);
        int cardPower = cardsInHand.get(player).getHoldingCard();
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
      }
    } else if(winsNeeded == 1) {
      gameIsOver = true;
    }

    return RoundStatusResponse.builder()
            .winner(winningPlayer)
            .roundOver(true)
            .gameOver(gameIsOver)
            .winningCard(gameIsOver ? Card.fromPower(cardsInHand.get(winningPlayer).getCardInHand()) : null)
            .build();
  }

  @Override
  public StartRoundResponse startRound() {
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
    final List<GamePlayer> playerList = new ArrayList<>();
    final List<Card> cardList = new ArrayList<>();
    for (var player : players) {
      int drawnCard = cardStack.drawCard();
      playedCards.put(player, new ArrayList<>());
      cardsInHand.put(player, new HandOfCards(drawnCard));
      players.get(players.indexOf(player)).setIsSafe(false);
      players.get(players.indexOf(player)).setIsOut(false);
      player.setIsOut(false);
      player.setIsSafe(false);
      playersInRound.add(player);
      playerList.add(player);
      cardList.add(Card.fromPower(drawnCard));
    }
    StartRoundResponse startRoundResponse = new StartRoundResponse(playerList, cardList);
    startRoundResponse.setCardKeptOut(Card.fromPower(cardStack.getCardKeptOut()));
    return startRoundResponse;
  }

  @Override
  synchronized public void removePlayerFromRound(GamePlayer playerToRemove) {
    var index = playersInRound.indexOf(playerToRemove);
    if (index < turnIndex) {
      turnIndex -= 1;
    }
    players.get(players.indexOf(playerToRemove)).setIsOut(true);
    playersInRound.get(index).setIsOut(true);
    playersInRound.remove(playerToRemove);
    Integer cardInHand;
    if ((cardInHand = cardsInHand.get(playerToRemove).getCardInHand()) != -1) {
      cardsInHand.get(playerToRemove).playedCard(cardInHand);
      playedCards.get(playerToRemove).add(Card.fromPower(cardInHand));
    }
    if ((cardInHand = cardsInHand.get(playerToRemove).getCardInHand()) != -1) {
      cardsInHand.get(playerToRemove).playedCard(cardInHand);
      playedCards.get(playerToRemove).add(Card.fromPower(cardInHand));
    }
  }

  @Override
  public GamePlayer nextTurn() {
    if (turnIndex >= playersInRound.size()) {
      turnIndex = 0;
    }
    GamePlayer player = playersInRound.get(turnIndex);
    player.setIsSafe(false);
    turnIndex += 1;
    return player;
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
    player.setIsOut(false);
    player.setIsSafe(false);
    player.setReady(false);

    numberOfPlayers += 1;
    players.add(player);
  }

  @Override
  synchronized public Boolean removePlayer(GamePlayer player) {
    boolean success = players.remove(player);

    if(success) {
      numberOfPlayers -= 1;
      numberOfReadyPlayers = 0;
      for (var p : players) {
        p.setReady(false);
      }

      return true;
    }

    return false;
  }

  @Override
  synchronized public GameAction syncPlayer(GamePlayer player) {
    numberOfPlayersSynced += 1;
    if (numberOfPlayersSynced == players.size()) {
      GameAction actionToTake = switch (gameState) {
        case WAITING_TO_START ->  {
          gameState = GameState.IN_PROGRESS;
          yield GameAction.START_ROUND;
        }
        case IN_PROGRESS -> {
          if(roundIsOver || gameIsOver) {
            gameState = GameState.ROUND_OVER;
          }
          yield GameAction.NEXT_TURN;
        }
        case ROUND_OVER, GAME_OVER -> {
          gameState = GameState.WAITING_TO_START;
          yield GameAction.ROUND_END;
        }
      };
      numberOfPlayersSynced = 0;
      return actionToTake;
    }

    // Not everyone ready, take no action
    return GameAction.NOTHING;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Metadata:    RoomKey: ").append(roomKey).append(", MaxPlayers: ").append(maxPlayers);
    stringBuilder.append(", MinPlayers: ").append(minPlayers).append(", NumberOfPlayers: ").append(numberOfPlayers);
    stringBuilder.append(", NumberOfReadyPlayers: ").append(numberOfReadyPlayers).append(", TurnIndex: ").append(turnIndex);
    stringBuilder.append(", NumberOfPlayersLoadedIn: ").append(numberOfPlayersSynced).append(", GameIsOver: ").append(gameIsOver).append("\n");
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
