package com.loot.server.ServiceTests;

import com.loot.server.domain.request.CreateGameRequest;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.GameInteractionRequest;
import com.loot.server.domain.request.JoinGameRequest;

import java.util.UUID;

public class ErrorCheckingServiceTestsUtil {

  // Create Game Request
  public static CreateGameRequest validCreateGameRequest() {
    return CreateGameRequest.builder()
            .roomName("My New Room")
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
            .roomName("My New Room")
            .build();
  }
  public static CreateGameRequest createGameRequestWithInvalidRoomName() {
    return CreateGameRequest.builder()
            .roomName("")
            .player(makeValidGamePlayer())
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
