package com.loot.server.ServiceTests;

import com.loot.server.domain.request.*;

import java.util.UUID;

public class ErrorCheckingServiceTestsUtil {

  // Create Game Request
  public static CreateGameRequest validCreateGameRequest() {
    return CreateGameRequest.builder()
            .settings(createGameSettings())
            .player(makeValidGamePlayer())
            .build();
  }
  public static CreateGameRequest createGameRequestMissingRoomName() {
    return CreateGameRequest.builder()
            .player(makeValidGamePlayer())
            .build();
  }
  public static CreateGameRequest createGameRequestMissingPlayer() {
    return CreateGameRequest.builder()
            .settings(createGameSettings())
            .build();
  }
  public static CreateGameRequest createGameRequestWithInvalidRoomName() {
    return CreateGameRequest.builder()
            .settings(createGameSettingsMissingBadRoomName())
            .player(makeValidGamePlayer())
            .build();
  }
  public static GameSettings createGameSettings() {
    return GameSettings.builder()
            .roomName("My New Room")
            .numberOfWinsNeeded(5)
            .numberOfPlayers(4)
            .build();
  }
  private static GameSettings createGameSettingsMissingBadRoomName() {
    return GameSettings.builder()
            .roomName("")
            .numberOfWinsNeeded(5)
            .numberOfPlayers(4)
            .build();
  }
  // Game Player
  public static GamePlayer makeValidGamePlayer() {
    return GamePlayer.builder()
            .name("Player 1")
            .id(UUID.randomUUID())
            .build();
  }
  public static GamePlayer gamePlayerMissingName() {
    return GamePlayer.builder()
            .id(UUID.randomUUID())
            .build();
  }
  public static GamePlayer gamePlayerMissingId() {
    return GamePlayer.builder()
            .name("Player 1")
            .build();
  }

  // Join Game Request
  public static JoinGameRequest validJoinGameRequest(String roomKey) {
    return JoinGameRequest.builder()
            .player(makeValidGamePlayer())
            .roomKey(roomKey)
            .build();
  }
  public static JoinGameRequest joinGameRequestMissingPlayer(String roomKey) {
    return JoinGameRequest.builder()
            .roomKey(roomKey)
            .build();
  }
  public static JoinGameRequest joinGameRequestMissingRoomKey() {
    return JoinGameRequest.builder()
            .player(makeValidGamePlayer())
            .build();
  }
  public static JoinGameRequest joinGameRequestWrongRoomKey() {
    return JoinGameRequest.builder()
            .player(makeValidGamePlayer())
            .roomKey("WRONG_KEY")
            .build();
  }
}
