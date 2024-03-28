package com.loot.server.ServiceTests;

import com.loot.server.domain.request.CreateGameRequest;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.GameInteractionRequest;
import com.loot.server.domain.request.JoinGameRequest;
import com.loot.server.service.ErrorCheckingService;
import com.loot.server.service.GameControllerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class TestForErrorCheckingService {

  @Autowired
  private ErrorCheckingService errorCheckingService;
  @Autowired
  private GameControllerService gameControllerService;

  @Test
  public void testsForCreateGameRequest() {
    CreateGameRequest requestToTest;
    Boolean hasError;

    requestToTest = ErrorCheckingServiceTestsUtil.validCreateGameRequest();
    hasError = errorCheckingService.requestContainsError(requestToTest);
    assert !hasError;

    requestToTest = ErrorCheckingServiceTestsUtil.createGameRequestMissingRoomName();
    hasError = errorCheckingService.requestContainsError(requestToTest);
    assert hasError;

    requestToTest = ErrorCheckingServiceTestsUtil.createGameRequestMissingPlayer();
    hasError = errorCheckingService.requestContainsError(requestToTest);
    assert hasError;

    requestToTest = ErrorCheckingServiceTestsUtil.createGameRequestWithInvalidRoomName();
    hasError = errorCheckingService.requestContainsError(requestToTest);
    assert hasError;
  }

  @Test
  public void testsForJoinGameRequest() {
    // Mock create the game request
    String roomName = "My New Room";
    GamePlayer gamePlayer = GamePlayer.builder().name("Host").id(UUID.randomUUID()).build();
    CreateGameRequest createGameRequest = CreateGameRequest.builder().player(gamePlayer).roomName(roomName).build();
    String roomKeyOfCreatedGame = createNewGame(createGameRequest);

    JoinGameRequest request;
    Boolean hasError;

    request = ErrorCheckingServiceTestsUtil.validJoinGameRequest(roomKeyOfCreatedGame);
    hasError = errorCheckingService.requestContainsError(request);
    assert !hasError;

    request = ErrorCheckingServiceTestsUtil.joinGameRequestMissingPlayer(roomKeyOfCreatedGame);
    hasError = errorCheckingService.requestContainsError(request);
    assert hasError;

    request = ErrorCheckingServiceTestsUtil.joinGameRequestWrongRoomKey();
    hasError = errorCheckingService.requestContainsError(request);
    assert hasError;

    request = ErrorCheckingServiceTestsUtil.joinGameRequestMissingRoomKey();
    hasError = errorCheckingService.requestContainsError(request);
    assert hasError;
  }

  @Test
  public void testForGamePlayerRequest() {
    GamePlayer gamePlayer;
    Boolean hasError;

    gamePlayer = ErrorCheckingServiceTestsUtil.makeValidGamePlayer();
    hasError = errorCheckingService.requestContainsError(gamePlayer);
    assert !hasError;

    gamePlayer = ErrorCheckingServiceTestsUtil.gamePlayerMissingId();
    hasError = errorCheckingService.requestContainsError(gamePlayer);
    assert hasError;

    gamePlayer = ErrorCheckingServiceTestsUtil.gamePlayerMissingName();
    hasError = errorCheckingService.requestContainsError(gamePlayer);
    assert hasError;
  }

  private String createNewGame(CreateGameRequest createGameRequest) {
    return gameControllerService.createNewGameSession(createGameRequest, UUID.randomUUID().toString());
  }
}
