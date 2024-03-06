package com.loot.server.GameSessionTests;

import com.loot.server.domain.GamePlayer;
import com.loot.server.socket.logic.GameSession;

import java.util.ArrayList;
import java.util.List;

public class GameSessionTestsUtil {

    public static List<GamePlayer> createPlayers() {
        return List.of(
                GamePlayer.builder().name("Player 1").id(1L).build(),
                GamePlayer.builder().name("Player 2").id(2L).build(),
                GamePlayer.builder().name("Player 3").id(3L).build(),
                GamePlayer.builder().name("Player 4").id(4L).build()
        );
    }

    private static void addPlayersToGameRoom(GameSession gameSession, int numberOfPlayersToAdd) {
        if(numberOfPlayersToAdd < 1 || numberOfPlayersToAdd > 4) {
            return;
        }

        List<GamePlayer> gamePlayerList = createPlayers();
        for(int i = 0; i < numberOfPlayersToAdd; i++) {
            gameSession.addPlayer(gamePlayerList.get(i));
        }
    }

    public static GameSession createGameRoom(String roomKey, int numberOfPlayers) {
        GameSession gameSession = new GameSession(roomKey);
        addPlayersToGameRoom(gameSession, numberOfPlayers);
        return gameSession;
    }

    public static GameSession createReadyLobby(String roomKey) {
        GameSession gameSession = new GameSession(roomKey);
        createPlayers().forEach(gameSession::addPlayer);
        List<GamePlayer> playerListCopy = new ArrayList<>(createPlayers());
        playerListCopy.forEach(gamePlayer -> {
            gamePlayer.setReady(true);
            gameSession.changePlayerReadyStatus(gamePlayer);
        });

        return gameSession;
    }

    public static GameSession createStartedGame(String roomKey) {
        GameSession gameSession = createReadyLobby(roomKey);
        List<GamePlayer> copyOfPlayers = new ArrayList<>();
        gameSession.getPlayers().forEach(player -> {
            var copy = player.copy(); copyOfPlayers.add(copy);
        });
        for (var copyOfPlayer : copyOfPlayers) {
            gameSession.loadedIntoGame(copyOfPlayer);
        }
        gameSession.startRound();
        return gameSession;
    }
}
