package com.loot.server.service.impl;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import com.loot.server.domain.request.CreateGameRequest;
import com.loot.server.domain.request.GamePlayer;
import com.loot.server.domain.request.GameInteractionRequest;
import com.loot.server.domain.request.JoinGameRequest;
import com.loot.server.domain.response.ErrorResponse;
import com.loot.server.service.ErrorCheckingService;
import com.loot.server.service.GameControllerService;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ErrorCheckingServiceImpl implements ErrorCheckingService {
  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;
  @Autowired
  private GameControllerService gameControllerService;

  private enum RequestErrorType {
    MISSING_PARAMS,
    INVALID_ROOM_NAME,
    INVALID_ROOM_KEY,
    UNKNOWN_REQUEST,
    UNABLE_TO_JOIN_GAME,
    MISSING_ROOM_KEY,
    BAD_REQUEST,
    NONE
  }

  @Override
  public Boolean requestContainsError(Object object) {
    if(object == null) {
      return true;
    }

    Pair<RequestErrorType, UUID> errorUUIDPair;
    if(object instanceof CreateGameRequest createGameRequest) {
      errorUUIDPair = createGameRequestContainsError(createGameRequest);
    } else if (object instanceof GameInteractionRequest gameInteractionRequest) {
      errorUUIDPair = gameInteractionRequestContainsError(gameInteractionRequest);
    } else if (object instanceof JoinGameRequest joinGameRequest) {
      errorUUIDPair = joinGameRequestContainsError(joinGameRequest);
    }else if (object instanceof GamePlayer gamePlayer) {
      RequestErrorType errorType = gamePlayerContainsError(gamePlayer) ? RequestErrorType.MISSING_PARAMS : RequestErrorType.NONE;
      errorUUIDPair = Pair.of(errorType, gamePlayer.getId());
    } else {
      errorUUIDPair = Pair.of(RequestErrorType.BAD_REQUEST, null);
    }
    if(errorUUIDPair.getLeft() != RequestErrorType.NONE) {
      sendErrorMessage(errorUUIDPair);
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  private Pair<RequestErrorType, UUID> createGameRequestContainsError(CreateGameRequest createGameRequest) {
    UUID clientID;
    if(createGameRequest.getRoomName() == null || createGameRequest.getPlayer() == null) {
      clientID = createGameRequest.getPlayer() == null ? null : createGameRequest.getPlayer().getId();
      return Pair.of(RequestErrorType.MISSING_PARAMS, clientID);
    }

    clientID = createGameRequest.getPlayer().getId();
    if(gamePlayerContainsError(createGameRequest.getPlayer())) {
      return Pair.of(RequestErrorType.MISSING_PARAMS, clientID);
    }
    if(createGameRequest.getRoomName().isEmpty()) {
      return Pair.of(RequestErrorType.MISSING_ROOM_KEY, clientID);
    }
    return Pair.of(RequestErrorType.NONE, clientID);
  }

  private Pair<RequestErrorType, UUID> gameInteractionRequestContainsError(GameInteractionRequest gameInteractionRequest) {
    UUID clientID;
    if(gameInteractionRequest.getPlayer() == null || gameInteractionRequest.getRoomKey() == null) {
      clientID = gameInteractionRequest.getPlayer() == null ? null : gameInteractionRequest.getPlayer().getId();
      return Pair.of(RequestErrorType.MISSING_PARAMS, clientID);
    }

    clientID = gameInteractionRequest.getPlayer().getId();
    if(gamePlayerContainsError(gameInteractionRequest.getPlayer())) {
      return Pair.of(RequestErrorType.MISSING_PARAMS, clientID);
    }
    return Pair.of(RequestErrorType.NONE, clientID);
  }

  private Pair<RequestErrorType, UUID> joinGameRequestContainsError(JoinGameRequest joinGameRequest) {
    GameInteractionRequest gameInteractionRequest = GameInteractionRequest.builder()
            .player(joinGameRequest.getPlayer())
            .roomKey(joinGameRequest.getRoomKey())
            .build();
    Pair<RequestErrorType, UUID> pair = gameInteractionRequestContainsError(gameInteractionRequest);
    if(pair.getLeft() != RequestErrorType.NONE) {
      return pair;
    }

    UUID clientID = joinGameRequest.getPlayer().getId();
    String roomKey = joinGameRequest.getRoomKey();
    if(!gameControllerService.gameExists(roomKey)) {
      return Pair.of(RequestErrorType.INVALID_ROOM_KEY, clientID);
    }
    if(!gameControllerService.gameAbleToBeJoined(roomKey)) {
      return Pair.of(RequestErrorType.UNABLE_TO_JOIN_GAME, clientID);
    }
    return Pair.of(RequestErrorType.NONE, clientID);
  }

  private Boolean gamePlayerContainsError(GamePlayer gamePlayer) {
    return (gamePlayer.getName() == null || gamePlayer.getId() == null);
  }

  private void sendErrorMessage(Pair<RequestErrorType, UUID> pair) {
    if(pair.getRight() == null) return;

    String channelToSendTo = "/topic/error/" + pair.getRight();
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.details = switch (pair.getLeft()) {
      case MISSING_PARAMS -> "Bad request. Please try again.";
      case INVALID_ROOM_NAME -> "The entered room name is invalid.";
      case INVALID_ROOM_KEY -> "The room could not be found, meaning it is likely no longer available. Please refresh the server list.";
      case UNKNOWN_REQUEST -> "The request you made is unknown. Fatal error.";
      case UNABLE_TO_JOIN_GAME -> "Cannot join game. It may be full or in progress, refresh server list to get updated status.";
      case MISSING_ROOM_KEY -> "Room key missing in request, make sure it is included.";
      case BAD_REQUEST -> "The given request type is unknown, consult with the backend team to resolve this error.";
      case NONE -> "N/A";
    };
    simpMessagingTemplate.convertAndSend(channelToSendTo, errorResponse);
  }
}
