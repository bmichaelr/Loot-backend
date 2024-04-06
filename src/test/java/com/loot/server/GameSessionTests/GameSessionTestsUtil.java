package com.loot.server.GameSessionTests;

import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.GameSettings;
import com.loot.server.logic.impl.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameSessionTestsUtil {

    private static final UUID p1UUID = UUID.randomUUID();
    private static final UUID p2UUID = UUID.randomUUID();
    private static final UUID p3UUID = UUID.randomUUID();
    private static final UUID p4UUID = UUID.randomUUID();


    public static List<GamePlayer> createPlayers() {
        return List.of(
                GamePlayer.builder().name("Player 1").id(p1UUID).build(),
                GamePlayer.builder().name("Player 2").id(p2UUID).build(),
                GamePlayer.builder().name("Player 3").id(p3UUID).build(),
                GamePlayer.builder().name("Player 4").id(p4UUID).build()
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
        GameSession gameSession = new GameSession(roomKey, createGameSettings());
        addPlayersToGameRoom(gameSession, numberOfPlayers);
        return gameSession;
    }

    public static GameSession createReadyLobby(String roomKey) {
        GameSession gameSession = new GameSession(roomKey, createGameSettings());
        createPlayers().forEach(gameSession::addPlayer);
        List<GamePlayer> playerListCopy = new ArrayList<>(createPlayers());
        playerListCopy.forEach(gamePlayer -> {
            gamePlayer.setReady(true);
            gameSession.changePlayerReadyStatus(gamePlayer);
        });

        return gameSession;
    }
    public static GameSettings createGameSettings() {
        return GameSettings.builder()
                .roomName("My New Room")
                .numberOfWinsNeeded(5)
                .numberOfPlayers(4)
                .build();
    }
    public static GameSession createStartedGame(String roomKey) {
        GameSession gameSession = createReadyLobby(roomKey);
        List<GamePlayer> copyOfPlayers = new ArrayList<>();
        gameSession.getPlayers().forEach(player -> {
            var copy = player.copy(); copyOfPlayers.add(copy);
        });
        for (var copyOfPlayer : copyOfPlayers) {
            gameSession.syncPlayer(copyOfPlayer);
        }
        gameSession.startRound();
        return gameSession;
    }
}
