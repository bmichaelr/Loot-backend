package com.loot.server.domain;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.cardresults.*;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.response.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ObjectDecodingTestsHelper {

    private static final Random random = new Random();

    public static GamePlayer mockGamePlayer() {
        return GamePlayer.builder()
                .name("Mock Player " + random.nextInt())
                .id(UUID.randomUUID())
                .isOut(false)
                .isSafe(false)
                .ready(false)
                .build();
    }

    public static LobbyResponse mockLobbyResponse() {
        return LobbyResponse.builder()
                .roomKey("123456")
                .name("Ben's room")
                .allReady(false)
                .players(List.of(mockGamePlayer(), mockGamePlayer(), mockGamePlayer(), mockGamePlayer()))
                .build();
    }

    public static List<ServerData> mockServersResponse() {
        return List.of(
                ServerData.builder().name("Test1").key("ABC123").maximumPlayers(4).numberOfPlayers(4).status("FULL").build(),
                ServerData.builder().name("Test2").key("123456").maximumPlayers(4).numberOfPlayers(3).status("AVAILABLE").build(),
                ServerData.builder().name("Test3").key("789213").maximumPlayers(4).numberOfPlayers(4).status("IN PROGRESS").build(),
                ServerData.builder().name("Test4").key("567567").maximumPlayers(4).numberOfPlayers(4).status("FULL").build()
        );
    }

    public static StartRoundResponse mockStartRoundResponse() {
        return StartRoundResponse.builder()
                .playersAndCards(
                        List.of(
                            PlayerCardPair.builder().player(mockGamePlayer()).card(Card.maulRat()).build(),
                            PlayerCardPair.builder().player(mockGamePlayer()).card(Card.pottedPlant()).build(),
                            PlayerCardPair.builder().player(mockGamePlayer()).card(Card.loot()).build(),
                            PlayerCardPair.builder().player(mockGamePlayer()).card(Card.duckOfDoom()).build()
                    )
                )
                .build();
    }

    public static NextTurnResponse mockNextTurnResponse() {
        return NextTurnResponse.builder()
                .player(mockGamePlayer())
                .card(Card.pottedPlant())
                .build();
    }

    public static RoundStatusResponse mockRoundStatusResponse() {
        return RoundStatusResponse.builder()
                .winner(mockGamePlayer())
                .roundOver(true)
                .gameOver(false)
                .build();
    }

    public static PlayedCardResponse mockPlayedCardResponse() {
        // There are six different types of BaseCardResult that could be included as the outcome
        int outcomeToSend = random.nextInt(1, 7);
        BaseCardResult outcome = switch (outcomeToSend) {
            case 1 -> new BaseCardResult(mockGamePlayer());
            case 2 -> new PottedResult(mockGamePlayer(), Card.duckOfDoom(), false);
            case 3 -> new MaulRatResult(mockGamePlayer(), Card.loot());
            case 4 -> new DuckResult(mockGamePlayer(), Card.netTroll(), Card.pottedPlant(), mockGamePlayer());
            case 5 -> new NetTrollResult(mockGamePlayer(), Card.wishingRing(), Card.duckOfDoom());
            case 6 -> new GazeboResult(mockGamePlayer(), Card.pottedPlant(), Card.turboniumDragon());
            default -> throw new RuntimeException("Invalid number passed to outcome switch!");
        };
        return PlayedCardResponse.builder()
                .playerWhoPlayed(mockGamePlayer())
                .cardPlayed(Card.pottedPlant())
                .waitFlag(false)
                .outcome(outcome)
                .build();
    }
}
