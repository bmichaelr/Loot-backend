package com.loot.server.GameSessionTests;

import com.loot.server.domain.GamePlayer;
import com.loot.server.socket.logic.GameSession;
import com.loot.server.socket.logic.cards.Card;
import com.loot.server.socket.logic.cards.impl.GuessingCard;
import com.loot.server.socket.logic.cards.impl.PlayedCard;
import com.loot.server.socket.logic.cards.impl.TargetedEffectCard;
import org.junit.jupiter.api.Test;

import java.util.Random;

// To test the logic
public class TestGameLogic {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    GameSession gameSession;
    Random rand = new Random();

    @Test
    public void testThatGameLogicIsWorkingAsExpected() {
        gameSession = GameSessionTestsUtil.createStartedGame("GAMEKEY1");

        // Since the game is started. We need to verify a few things
        assert gameSession.getPlayersInRound() != null;
        assert gameSession.getCardsInHand() != null;
        assert gameSession.getPlayedCards() != null;
        // Verify that each player has a hand
        var cardInHandMap = gameSession.getCardsInHand();
        for(var hand : cardInHandMap.values()) {
            assert hand.getCardInHand() != -1;
        }

        while(!gameSession.isGameIsOver()) {
            var player = gameSession.nextTurn();
            gameSession.dealCard(player);
            var handOfCards = gameSession.getCardsInHand().get(player);
            System.out.println(ANSI_RED + "ROUND ITERATION: " + ANSI_RESET + " Player playing = " + ANSI_BLUE + player.getName() + ANSI_RESET + ", Card In Hand = " + ANSI_BLUE + handOfCards.getCardInHand() + ANSI_RESET + ", Drawn Card: " + ANSI_BLUE + handOfCards.getDrawnCard() + ANSI_RESET);
            assert handOfCards.hasTwoCards();
            PlayedCard cardToPlay = createPlayedCardObjectHelper(player);
            assert validateCardPlayed(player, cardToPlay);
        }

        if(gameSession.getPlayersInRound().size() > 1) {
            var index = 0;
            var maxCard = 0;
            for(int i = 0; i < gameSession.getPlayersInRound().size(); ++i) {
                var player = gameSession.getPlayersInRound().get(i);
                var cardPower = gameSession.getCardsInHand().get(player).getHoldingCard();
                if(cardPower > maxCard) {
                    maxCard = cardPower;
                    index = i;
                }
            }
            System.out.println(ANSI_GREEN + "THE PLAYER WHO WON = " + gameSession.getPlayersInRound().get(index) + ANSI_RESET);
        } else {
            System.out.println(ANSI_GREEN + "THE PLAYER WHO WON = " + gameSession.getPlayersInRound().get(0) + ANSI_RESET);
        }
    }

    private PlayedCard createPlayedCardObjectHelper(GamePlayer currentlyPlaying) {
        // Randomly choose to play either the card in hand or the drawn card
        var handOfCards = gameSession.getCardsInHand().get(currentlyPlaying);
        int cardToPlay = rand.nextBoolean() ? handOfCards.getHoldingCard() : handOfCards.getDrawnCard();
        //handOfCards.playedCard(cardToPlay);

        PlayedCard playedCard;
        if(isSingleActionCard(cardToPlay)) {
            playedCard = new PlayedCard(cardToPlay);
        } else if(isTargetedCard(cardToPlay)) {
            playedCard = new TargetedEffectCard(cardToPlay, randomPlayer(currentlyPlaying, cardToPlay == 5));
        } else { // It is the potted plant, e.g. the guessing card
            playedCard = new GuessingCard(cardToPlay, rand.nextInt(2,9), randomPlayer(currentlyPlaying, false));
        }
        System.out.println(ANSI_YELLOW + "The card being played:");
        if(playedCard instanceof GuessingCard guessingCard) {
            System.out.println("\tGUESS CARD: power = " + guessingCard.getPower() + ", card guessed = " + guessingCard.getGuessedCard() + ", guessed on = " + guessingCard.getGuessedOn().getName());
        } else if (playedCard instanceof TargetedEffectCard targetedEffectCard) {
            System.out.println("TARGETED CARD: power = " + targetedEffectCard.getPower() + ", played on = " + targetedEffectCard.getPlayedOn().getName());
        } else {
            System.out.println("NORMAL CARD: power = " + playedCard.getPower());
        }
        System.out.println(ANSI_RESET);
        return playedCard;
    }


    private boolean isSingleActionCard(int cardPower) {
        return cardPower == 7 || cardPower == 8 || cardPower == 4;
    }

    private boolean isTargetedCard(int cardPower) {
        return cardPower == 2 || cardPower == 3 || cardPower == 6 || cardPower == 5;
    }

    /**
     * Helper function to get a random player to guess on
     * @param currentPlayer the player who is playing the card
     * @param playableOnSelf boolean indicating if the card is playable on the players self
     * @return a (pseudo)randomly chose player
     */
    private GamePlayer randomPlayer(GamePlayer currentPlayer, boolean playableOnSelf) {
        var playersToChooseFrom = gameSession.getPlayersInRound();
        int amountOfPlayers = playersToChooseFrom.size();
        GamePlayer randomPlayer;
        do {
            randomPlayer = playersToChooseFrom.get(rand.nextInt(amountOfPlayers));
        } while(randomPlayer == currentPlayer && !playableOnSelf);

        return randomPlayer;
    }

    // Massive function, not even going to bother with refactoring as this is a test function
    private Boolean validateCardPlayed(GamePlayer player, PlayedCard card) {
        var cih = gameSession.getCardsInHand();
        var pir = gameSession.getPlayersInRound();
        var ppc = gameSession.getPlayedCards();
        var otherCardInHand = (cih.get(player).getHoldingCard().equals(card.getPower())) ? cih.get(player).getDrawnCard() : cih.get(player).getHoldingCard();

        switch(card.getPower()) {
            case 1 -> {
                var guessedPlayer = ((GuessingCard) card).getGuessedOn();
                var guessedCard = ((GuessingCard) card).getGuessedCard();
                var cardInHandOfGuess = cih.get(guessedPlayer).getCardInHand();
                var correctGuess = cardInHandOfGuess.equals(guessedCard);
                gameSession.playCard(player, card);
                if(correctGuess) {
                    // Validate the player was removed from the players in round list and the card they had is in the
                    // list of played cards for that person
                    assert !pir.contains(guessedPlayer) : assertionLog("correct potted guess resulted in opponent being removed from in round list");
                    assert ppc.get(guessedPlayer).contains(Card.fromPower(cardInHandOfGuess)) : assertionLog("correct potted guess resulted in the card the person had now being in their played cards list");
                } else {
                    assert pir.contains(guessedPlayer) : assertionLog("incorrect potted guess means the opponent is still in the round list");
                }
            }
            case 3 -> {
                var playedOn = ((TargetedEffectCard) card).getPlayedOn();
                assert pir.contains(playedOn) : assertionLog("the target player for duck of doom was not found in the list!");
                var playedOnCard = cih.get(playedOn).getHoldingCard();
                var playerHasHigherCard = otherCardInHand >= playedOnCard;
                gameSession.playCard(player, card);
                if(playerHasHigherCard) {
                    if(otherCardInHand.equals(playedOnCard)) {
                        assert pir.contains(player) : assertionLog("on duck of doom tie, the player who played is not in the round");
                        assert pir.contains(playedOn) : assertionLog("on duck of doom tie, the player played on is not in round");
                        assert cih.get(player).getHoldingCard().equals(otherCardInHand) : assertionLog("on duck of doom tie, the players card is not the same");
                        assert cih.get(playedOn).getHoldingCard().equals(playedOnCard) : assertionLog("on duck of doom tie, the opponents card is not the same");
                    } else {
                        assert !pir.contains(playedOn) : assertionLog("on player win duck of doom, the opponent is still in the round (shouldn't be)");
                        assert pir.contains(player) : assertionLog("on player won duck of doom, the player is not in the round (should be)");
                        assert ppc.get(playedOn).contains(Card.fromPower(playedOnCard)) : assertionLog("on player won duck of doom, opponents card is not in their played list");
                    }
                } else {
                    assert !pir.contains(player) : assertionLog("on player lost duck of doom, they are still in the round");
                    assert pir.contains(playedOn) : assertionLog("on player lost duck of doom, opponent is not in the round (should be)");
                }
            }
            case 2 -> {
                var playedOn = ((TargetedEffectCard) card).getPlayedOn();
                gameSession.playCard(player, card);
                assert pir.contains(playedOn) : assertionLog("on maul rat, the opponent is no longer in the round (should be)");

            }
            case 4 -> {
                gameSession.playCard(player, card);
                assert pir.contains(player) : assertionLog("on wishing ring, player is no longer in the round (should be)");
                assert pir.get(pir.indexOf(player)).getIsSafe() : assertionLog("on wishing ring, player is not marked as safe (should be)");
            }
            case 5 -> {
                var playedOn = ((TargetedEffectCard) card).getPlayedOn();
                var playedOnCard = cih.get(playedOn).getHoldingCard();
                gameSession.playCard(player, card);
                assert ppc.get(playedOn).contains(Card.fromPower(playedOnCard)) : assertionLog("on net troll, the opponent's discarded card is not in their list of layed cards");
                //assert ppc.get(playedOn).contains(Card.fromPower(playedOnCard)) : assertionLog("on net troll, the opponents new card is the same as the old one");
            }
            case 6 -> {
                var playedOn = ((TargetedEffectCard) card).getPlayedOn();
                var playedOnCard = cih.get(playedOn).getHoldingCard();
                gameSession.playCard(player, card);
                assert pir.contains(player) : assertionLog("on gazebo, the player is no longer in the round (should be)");
                assert pir.contains(playedOn) : assertionLog("on gazebo, the opponent is no longer in the round (should be)");
                assert cih.get(playedOn).getHoldingCard().equals(otherCardInHand) : assertionLog("on gazebo, the players hand stayed the same");
                assert cih.get(player).getHoldingCard().equals(playedOnCard) : assertionLog("on gazebo, the opponents hand stayed the same");
            }
            case 7 -> {
                gameSession.playCard(player, card);
                assert !cih.get(player).getHoldingCard().equals(card.getPower()) : assertionLog("player who discarded dragon should not have it in hand anymore");
            }
            case 8 -> {
                gameSession.playCard(player, card);
                assert !pir.contains(player) : assertionLog("player who discarded loot is no longer in the round");
            }
            default -> throw new IllegalStateException("Unexpected value: " + card.getPower());
        };

        assert ppc.get(player).contains(Card.fromPower(card.getPower())) : assertionLog("the played card is now in the players played cards list");
        turnLog(player, card);
        return true;
    }

    private void turnLog(GamePlayer player, PlayedCard card) {
        // Log some cool information
        System.out.println("Player playing this turn: " + player.getName() + ", playing card: " + card.getPower());
        System.out.println("GameSession Object Dump\n:" + gameSession);
    }

    private String assertionLog(String assertion) {
        return "Assertion failed : " + assertion;
    }

}
