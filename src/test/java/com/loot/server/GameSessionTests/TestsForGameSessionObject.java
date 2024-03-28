package com.loot.server.GameSessionTests;

import com.loot.server.domain.request.GameInteractionRequest;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.logic.impl.GameSession;
import com.loot.server.service.ErrorCheckingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class TestsForGameSessionObject {

    @Autowired
    private ErrorCheckingService errorCheckingService;

    @Test
    public void testThatAddingPlayersWorks() {
        GameSession gameSession = new GameSession("ABC123", "Boogers");
        List<GamePlayer> playersToAdd = GameSessionTestsUtil.createPlayers();
        playersToAdd.forEach(gameSession::addPlayer);

        // Run through an assertion loop to make sure that all the players are in the lobby
        for(var player : playersToAdd) {
            assert gameSession.getPlayers().contains(player);
        }
    }

    @Test
    public void testThatChangingReadyStatusWorks() {
        GameSession gameSession = GameSessionTestsUtil.createGameRoom("ABC123", 4);
        assert gameSession.getRoomKey().equals("ABC123");
        assert gameSession.getNumberOfPlayers() == 4;

        // Validate that all players are false when initially added
        for(var player : gameSession.getPlayers()) {
            assert player.getReady().equals(Boolean.FALSE);
        }

        GamePlayer player1 = gameSession.getPlayers().get(0).copy();
        GamePlayer player2 = gameSession.getPlayers().get(1).copy();
        GamePlayer player3 = gameSession.getPlayers().get(2).copy();
        GamePlayer player4 = gameSession.getPlayers().get(3).copy();
        Boolean allReady;
        System.out.println(player1.toString());

        // Test that the players ready status is changed and number of ready players equals 1
        player1.setReady(true);
        gameSession.changePlayerReadyStatus(player1);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player1)).getReady().equals(Boolean.TRUE);
        assert gameSession.getNumberOfReadyPlayers() == 1;

        // Test that we are able to unready the player
        player1.setReady(false);
        allReady = gameSession.changePlayerReadyStatus(player1);
        assert allReady.equals(Boolean.FALSE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player1)).getReady().equals(Boolean.FALSE);
        assert gameSession.getNumberOfReadyPlayers() == 0;

        // Test that nothing changes when trying to call change ready status with an already unready player
        player1.setReady(false);
        allReady = gameSession.changePlayerReadyStatus(player1);
        assert allReady.equals(Boolean.FALSE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player1)).getReady().equals(Boolean.FALSE);
        assert gameSession.getNumberOfReadyPlayers() == 0;

        // Test readying two players
        player1.setReady(true);
        player2.setReady(true);
        allReady = gameSession.changePlayerReadyStatus(player1);
        assert allReady.equals(Boolean.FALSE);
        allReady = gameSession.changePlayerReadyStatus(player2);
        assert allReady.equals(Boolean.FALSE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player1)).getReady().equals(Boolean.TRUE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player2)).getReady().equals(Boolean.TRUE);
        assert gameSession.getNumberOfReadyPlayers() == 2;

        // Ready up the third player
        player3.setReady(true);
        allReady = gameSession.changePlayerReadyStatus(player3);
        assert allReady.equals(Boolean.FALSE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player3)).getReady().equals(Boolean.TRUE);
        assert gameSession.getNumberOfReadyPlayers() == 3;

        // Unready the third player
        player3.setReady(false);
        allReady = gameSession.changePlayerReadyStatus(player3);
        assert allReady.equals(Boolean.FALSE);
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player3)).getReady().equals(Boolean.FALSE);
        assert gameSession.getNumberOfReadyPlayers() == 2;

        // Ready the last two players
        player3.setReady(true);
        player4.setReady(true);
        allReady = gameSession.changePlayerReadyStatus(player3);
        assert allReady.equals(Boolean.FALSE);
        allReady = gameSession.changePlayerReadyStatus(player4);
        assert allReady.equals(Boolean.TRUE);
        for(var player : gameSession.getPlayers()) {
            assert player.getReady().equals(Boolean.TRUE);
        }
        assert gameSession.getNumberOfReadyPlayers() == 4;
    }

    @Test
    public void testThatLobbiesWithVaryingSizesCanStartTheGameIfAllReady() {
        // Tests for a one player lobby
        GameSession onePlayerGame = GameSessionTestsUtil.createGameRoom("BBB222", 1);
        assert onePlayerGame.getNumberOfPlayers() == 1;

        GamePlayer player1forOnePlayerLobby = onePlayerGame.getPlayers().get(0).copy();
        player1forOnePlayerLobby.setReady(true);
        boolean ready = onePlayerGame.changePlayerReadyStatus(player1forOnePlayerLobby);
        assert onePlayerGame.getPlayers().get(onePlayerGame.getPlayers().indexOf(player1forOnePlayerLobby)).getReady().equals(Boolean.TRUE);
        assert onePlayerGame.getNumberOfReadyPlayers() == 1;
        assert !ready;


        // Tests for two player lobby
        GameSession twoPlayerGame = GameSessionTestsUtil.createGameRoom("BBB222", 2);
        assert twoPlayerGame.getNumberOfPlayers() == 2;

        GamePlayer player1ForTwoPlayerLobby = twoPlayerGame.getPlayers().get(0).copy();
        GamePlayer player2ForTwoPlayerLobby = twoPlayerGame.getPlayers().get(1).copy();

        // ready up the first player
        player1ForTwoPlayerLobby.setReady(true);
        ready = twoPlayerGame.changePlayerReadyStatus(player1ForTwoPlayerLobby);
        assert twoPlayerGame.getPlayers().get(twoPlayerGame.getPlayers().indexOf(player1ForTwoPlayerLobby)).getReady().equals(Boolean.TRUE);
        assert twoPlayerGame.getNumberOfReadyPlayers() == 1;
        assert !ready;

        // ready up the second player
        player2ForTwoPlayerLobby.setReady(true);
        ready = twoPlayerGame.changePlayerReadyStatus(player2ForTwoPlayerLobby);
        assert twoPlayerGame.getPlayers().get(twoPlayerGame.getPlayers().indexOf(player2ForTwoPlayerLobby)).getReady().equals(Boolean.TRUE);
        assert twoPlayerGame.getNumberOfReadyPlayers() == 2;
        assert ready;

        // Tests for three player lobby
        GameSession threePlayerGame = GameSessionTestsUtil.createGameRoom("BBB222", 3);
        assert threePlayerGame.getNumberOfPlayers() == 3;

        GamePlayer player1ForThreePlayerLobby = threePlayerGame.getPlayers().get(0).copy();
        GamePlayer player2ForThreePlayerLobby = threePlayerGame.getPlayers().get(1).copy();
        GamePlayer player3ForThreePlayerLobby = threePlayerGame.getPlayers().get(2).copy();

        // ready up the first player
        player1ForThreePlayerLobby.setReady(true);
        ready = threePlayerGame.changePlayerReadyStatus(player1ForThreePlayerLobby);
        assert threePlayerGame.getPlayers().get(threePlayerGame.getPlayers().indexOf(player1ForThreePlayerLobby)).getReady().equals(Boolean.TRUE);
        assert threePlayerGame.getNumberOfReadyPlayers() == 1;
        assert !ready;

        // ready up the second player
        player2ForThreePlayerLobby.setReady(true);
        ready = threePlayerGame.changePlayerReadyStatus(player2ForThreePlayerLobby);
        assert threePlayerGame.getPlayers().get(threePlayerGame.getPlayers().indexOf(player2ForThreePlayerLobby)).getReady().equals(Boolean.TRUE);
        assert threePlayerGame.getNumberOfReadyPlayers() == 2;
        assert !ready;

        // ready up the third and last player
        player3ForThreePlayerLobby.setReady(true);
        ready = threePlayerGame.changePlayerReadyStatus(player3ForThreePlayerLobby);
        assert threePlayerGame.getPlayers().get(threePlayerGame.getPlayers().indexOf(player3ForThreePlayerLobby)).getReady().equals(Boolean.TRUE);
        assert threePlayerGame.getNumberOfReadyPlayers() == 3;
        assert ready;
    }

    @Test
    public void testThatLoadingInPlayersWorks() {
        GameSession gameSession = GameSessionTestsUtil.createReadyLobby("ABC123");
        assert gameSession.getRoomKey().equals("ABC123");

        // Verify that the lobby is indeed ready
        for(var player : gameSession.getPlayers()) {
            assert player.getReady().equals(Boolean.TRUE);
        }
        assert gameSession.getNumberOfReadyPlayers() == 4;
        assert gameSession.getNumberOfPlayersLoadedIn() == 0;

        GamePlayer player1 = gameSession.getPlayers().get(0).copy();
        GamePlayer player2 = gameSession.getPlayers().get(1).copy();
        GamePlayer player3 = gameSession.getPlayers().get(2).copy();
        GamePlayer player4 = gameSession.getPlayers().get(3).copy();
        Boolean allLoadedIn;

        // Load in player 1
        player1.setLoadedIn(true);
        allLoadedIn = gameSession.loadedIntoGame(player1);
        assert allLoadedIn.equals(Boolean.FALSE);
        assert gameSession.getNumberOfPlayersLoadedIn() == 1;
        assert gameSession.getPlayers().get(gameSession.getPlayers().indexOf(player1)).getLoadedIn().equals(Boolean.TRUE);

        // Load in the rest of the players
        player2.setLoadedIn(true);
        player3.setLoadedIn(true);
        player4.setLoadedIn(true);
        allLoadedIn = gameSession.loadedIntoGame(player2);
        assert allLoadedIn.equals(Boolean.FALSE) && gameSession.getNumberOfPlayersLoadedIn() == 2;
        allLoadedIn = gameSession.loadedIntoGame(player3);
        assert allLoadedIn.equals(Boolean.FALSE) && gameSession.getNumberOfPlayersLoadedIn() == 3;
        allLoadedIn = gameSession.loadedIntoGame(player4);
        assert allLoadedIn.equals(Boolean.TRUE) && gameSession.getNumberOfPlayersLoadedIn() == 4;
    }

    @Test
    public void testThatStartRoundMethodInitializesAllDataStructures() {
        GameSession gameSession = GameSessionTestsUtil.createReadyLobby("IUI552");
        assert gameSession.getRoomKey().equals("IUI552");

        // Load in all the players to start the game
        List<GamePlayer> copyOfPlayers = new ArrayList<>();
        gameSession.getPlayers().forEach(player -> {
            var copy = player.copy(); copyOfPlayers.add(copy);
        });

        Boolean loadedIn;
        for(int i = 0; i < copyOfPlayers.size(); i++) {
            loadedIn = gameSession.loadedIntoGame(copyOfPlayers.get(i));
            assert (i == copyOfPlayers.size() - 1) == loadedIn;
        }

        gameSession.startRound();
        assert gameSession.getPlayersInRound() != null;
        assert gameSession.getCardsInHand() != null;
        assert gameSession.getPlayedCards() != null;
        assert gameSession.getCardStack() != null;
        assert gameSession.getPlayersInRound().size() == gameSession.getPlayers().size();
    }

    @Test
    public void testPlayerTurnCycling() {
        GameSession gameSession = GameSessionTestsUtil.createStartedGame("KEY898989");

        // get the players
        GamePlayer player1 = gameSession.getPlayers().get(0).copy();
        GamePlayer player2 = gameSession.getPlayers().get(1).copy();
        GamePlayer player3 = gameSession.getPlayers().get(2).copy();
        GamePlayer player4 = gameSession.getPlayers().get(3).copy();

        var player = gameSession.nextTurn();
        assert player.equals(player1);
        assert gameSession.getTurnIndex() == 1;

        player = gameSession.nextTurn();
        assert player.equals(player2);
        assert gameSession.getTurnIndex() == 2;

        player = gameSession.nextTurn();
        assert player.equals(player3);
        assert gameSession.getTurnIndex() == 3;

        player = gameSession.nextTurn();
        assert player.equals(player4);
        assert gameSession.getTurnIndex() == 4;

        // Should be back to player 1 now, remove player 2 make sure we go to player three
        player = gameSession.nextTurn();
        assert player.equals(player1);
        assert gameSession.getTurnIndex() == 1;
        gameSession.removePlayerFromRound(player2);
        player = gameSession.nextTurn();
        assert player.equals(player3);

        // now we have player1, player3, player4. remove player 4 and make sure we go back to player 1
        gameSession.removePlayerFromRound(player4);
        player = gameSession.nextTurn();
        assert player.equals(player1);
        assert gameSession.getPlayersInRound().size() == 2;
        player = gameSession.nextTurn();
        assert player.equals(player3);

        // Remove all but player one, should oscillate between player1
        gameSession.removePlayerFromRound(player3);
        player = gameSession.nextTurn();
        assert player.equals(player1);
        player = gameSession.nextTurn();
        assert player.equals(player1);

        // Add back all the players : next player should be player 2
        // player list = player1, player2, player3, player4
        gameSession.getPlayersInRound().addAll(List.of(player2, player3, player4));
        player = gameSession.nextTurn();
        assert player.equals(player2);

        // remove player 1, make sure we correctly cycle to player3, then 4, then 2
        gameSession.removePlayerFromRound(player1);
        player = gameSession.nextTurn();
        assert player.equals(player3);
        player = gameSession.nextTurn();
        assert player.equals(player4);
        player = gameSession.nextTurn();
        assert player.equals(player2);
    }

    @Test
    public void turnTest() {
        GameSession gameSession = GameSessionTestsUtil.createStartedGame("KEY898989");

        GamePlayer player1 = gameSession.getPlayers().get(0).copy();
        GamePlayer player2 = gameSession.getPlayers().get(1).copy();
        GamePlayer player3 = gameSession.getPlayers().get(2).copy();
        GamePlayer player4 = gameSession.getPlayers().get(3).copy();

        var player = gameSession.nextTurn();
        assert player.equals(player1);
        assert gameSession.getTurnIndex() == 1;

        player = gameSession.nextTurn();
        assert player.equals(player2);
        assert gameSession.getTurnIndex() == 2;

        player = gameSession.nextTurn();
        assert player.equals(player3);
        assert gameSession.getTurnIndex() == 3;
        gameSession.removePlayerFromRound(player);

        player = gameSession.nextTurn();
        assert player.equals(player4);
    }
}
