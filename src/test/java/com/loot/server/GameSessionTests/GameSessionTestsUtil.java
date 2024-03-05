package com.loot.server.GameSessionTests;

import com.loot.server.domain.GamePlayer;

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
}
