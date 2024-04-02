package com.loot.server.ControllerTest;

import com.loot.server.domain.request.CreateGameRequest;
import com.loot.server.domain.request.GameInteractionRequest;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.JoinGameRequest;
import lombok.Getter;

import java.util.UUID;

public class GameControllerTestUtil {

    @Getter
    static final String TEST_ROOM_NAME = "Test Room Name";
    @Getter
    static final String TEST_PLAYER_NAME = "TestPlayer";
    @Getter
    static final UUID TEST_PLAYER_UUID = UUID.randomUUID();

    public enum RequestType {
        VALID,
        MISSING_ROOM_NAME,
        WRONG_ROOM_KEY,
        MISSING_ROOM_KEY,
        MISSING_GAME_PLAYER,
        GAME_PLAYER_MISSING_NAME,
        GAME_PLAYER_MISSING_ID,
        RANDOM_PLAYER
    }

    public static GamePlayer createValidGamePlayer() {
        return GamePlayer.builder()
                .name(TEST_PLAYER_NAME)
                .id(TEST_PLAYER_UUID)
                .build();
    }

    public static GamePlayer createRandomPlayer() {
        return GamePlayer.builder()
                .name("Random Player")
                .id(UUID.randomUUID())
                .build();
    }

    public static GamePlayer createGamePlayerMissingName() {
        return GamePlayer.builder()
                .id(TEST_PLAYER_UUID)
                .build();
    }

    public static GamePlayer createGamePlayerMissingId() {
        return GamePlayer.builder()
                .name(TEST_PLAYER_NAME)
                .build();
    }

    public static CreateGameRequest createGameRequest(RequestType requestType) {
        if(requestType == RequestType.MISSING_GAME_PLAYER) {
            return CreateGameRequest.builder()
                    .roomName(TEST_ROOM_NAME)
                    .build();
        }
        if(requestType == RequestType.MISSING_ROOM_NAME) {
            return CreateGameRequest.builder()
                    .player(createValidGamePlayer())
                    .build();
        }
        if(requestType == RequestType.RANDOM_PLAYER) {
            return CreateGameRequest.builder()
                    .player(GamePlayer.builder()
                            .id(UUID.randomUUID())
                            .name("Rando")
                            .ready(false)
                            .isSafe(false)
                            .isOut(false)
                            .build())
                    .roomName(TEST_ROOM_NAME)
                    .build();
        }
        return CreateGameRequest
                .builder()
                .roomName(TEST_ROOM_NAME)
                .player(
                        switch(requestType) {
                            case VALID -> createValidGamePlayer();
                            case GAME_PLAYER_MISSING_NAME -> createGamePlayerMissingName();
                            case GAME_PLAYER_MISSING_ID -> createGamePlayerMissingId();
                            default -> null;
                        }
                )
                .build();
    }
    public static JoinGameRequest createJoinGameRequest(String roomKey, RequestType requestType) {
        return JoinGameRequest.builder()
                .player(switch (requestType) {
                    case VALID, MISSING_ROOM_NAME, MISSING_ROOM_KEY, WRONG_ROOM_KEY -> createValidGamePlayer();
                    case MISSING_GAME_PLAYER -> null;
                    case GAME_PLAYER_MISSING_NAME -> createGamePlayerMissingName();
                    case GAME_PLAYER_MISSING_ID -> createGamePlayerMissingId();
                    case RANDOM_PLAYER -> createRandomPlayer();
                })
                .roomKey(requestType == RequestType.MISSING_ROOM_KEY ? null : requestType == RequestType.WRONG_ROOM_KEY ? "WRONG_KEY" : roomKey)
                .build();
    }
    public static GameInteractionRequest createGameInteractionRequest(String roomKey, GamePlayer player) {
        return GameInteractionRequest.builder()
                .player(player)
                .roomKey(roomKey)
                .build();
    }
}
