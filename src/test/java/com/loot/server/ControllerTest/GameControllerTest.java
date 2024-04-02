package com.loot.server.ControllerTest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.GuessingCard;
import com.loot.server.domain.cards.PlayedCard;
import com.loot.server.domain.cards.TargetedEffectCard;
import com.loot.server.domain.cards.cardresults.*;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.*;
import com.loot.server.ControllerTest.GameControllerTestUtil.RequestType;
import lombok.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {

    @Getter
    public enum FrameHandlerType {
        AVAILABLE_SERVER_RESPONSE(ServerData.class),
        LOBBY_RESPONSE(LobbyResponse.class),
        START_ROUND_RESPONSE(StartRoundResponse.class),
        NEXT_TURN_RESPONSE(NextTurnResponse.class),
        ROUND_STATUS_RESPONSE(RoundStatusResponse.class),
        PLAYED_CARD_RESPONSE(PlayedCardResponse.class),
        ERROR_RESPONSE(ErrorResponse.class),
        STRING_RESPONSE(String.class);

        private final Class<?> classType;

        FrameHandlerType(Class<?> classType) {
            this.classType = classType;
        }
    }

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient webSocketStompClient;

    private String getWsPath() {
        return String.format("ws://localhost:%d/game-websocket", port);
    }

    @BeforeEach
    void setup() {
        webSocketStompClient =
                new WebSocketStompClient(
                        new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    // -- MARK: Fetch Available Servers Tests
    @Test
    void verifyThatFetchServersSendsMessageWhenCorrect() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        createRandomGame(wsTestHelper);
        GamePlayer player = GameControllerTestUtil.createValidGamePlayer();
        UUID playerId = player.getId();
        wsTestHelper.listenToChannel("/topic/matchmaking/servers/" + playerId, FrameHandlerType.AVAILABLE_SERVER_RESPONSE);
        wsTestHelper.sendToSocket("/app/loadAvailableServers", player);

        await().atMost(3, SECONDS).untilAsserted(() -> assertNotNull(wsTestHelper.pollQueue(FrameHandlerType.AVAILABLE_SERVER_RESPONSE)));
        await().atMost(3, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatFetchServersFailsWhenPlayerMissingName() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        createRandomGame(wsTestHelper);

        GamePlayer player = GameControllerTestUtil.createGamePlayerMissingName();
        wsTestHelper.listenToChannel("/topic/matchmaking/servers/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.AVAILABLE_SERVER_RESPONSE);
        wsTestHelper.sendToSocket("/app/loadAvailableServers", player);

        await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.AVAILABLE_SERVER_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatFetchServersFailsWhenPlayerMissingId() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        createRandomGame(wsTestHelper);

        GamePlayer player = GameControllerTestUtil.createGamePlayerMissingId();
        wsTestHelper.listenToChannel("/topic/matchmaking/servers/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.AVAILABLE_SERVER_RESPONSE);
        wsTestHelper.sendToSocket("/app/loadAvailableServers", player);

        await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.AVAILABLE_SERVER_RESPONSE)));
        wsTestHelper.shutdown();
    }

    // -- MARK: Create Game Tests
    @Test
    void verifyCreateGameMessageIsReceivedWhenValid() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.VALID);

        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));

        LobbyResponse lobbyResponse = (LobbyResponse) wsTestHelper.pollQueue(FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenMissingRoomName() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.MISSING_ROOM_NAME);

        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(2, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(2, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenMissingPlayer() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.MISSING_GAME_PLAYER);

        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenGamePlayerMissingName() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.GAME_PLAYER_MISSING_NAME);

        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenGamePlayerMissingId() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.GAME_PLAYER_MISSING_ID);

        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }

    // -- MARK: Join Game Tests
    @Test
    void verifyJoinGameMessageIsReceivedWhenValid() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameMSendsErrorWhenMissingRoomKey() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.MISSING_ROOM_KEY);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> {
            assertEquals("Bad request. Please try again.", ((ErrorResponse)wsTestHelper.pollQueue(FrameHandlerType.ERROR_RESPONSE)).details);
        });
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameFailsWhenMissingPlayer() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.MISSING_GAME_PLAYER);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameSendsErrorWhenWrongRoomKey() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.WRONG_ROOM_KEY);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        assertEquals("The room could not be found, meaning it is likely no longer available. Please refresh the server list.",
                ((ErrorResponse)wsTestHelper.pollQueue(FrameHandlerType.ERROR_RESPONSE)).details);
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameSendsErrorWhenLobbyFull() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        String roomKey = createRandomGameAndFillIt(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertEquals("Cannot join game. It may be full or in progress, refresh server list to get updated status.",
                        ((ErrorResponse)wsTestHelper.pollQueue(FrameHandlerType.ERROR_RESPONSE)).details)
        );
        wsTestHelper.shutdown();
    }

    @Test
    void verifyJoinGameSendsErrorWhenGameInProgress() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        

        final List<GamePlayer> playersInGame = new ArrayList<>();
        String roomKey = createRandomGameAndFillIt(wsTestHelper, playersInGame);
        startGame(wsTestHelper, playersInGame, roomKey);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(2, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertEquals("Cannot join game. It may be full or in progress, refresh server list to get updated status.",
                        ((ErrorResponse)wsTestHelper.pollQueue(FrameHandlerType.ERROR_RESPONSE)).details)
        );
        wsTestHelper.shutdown();
    }

    // -- MARK: Testing leave game endpoint
    @Test
    void verifyThatLeaveGameSendsMessageToOtherPlayers() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        final List<GamePlayer> players = new ArrayList<>();
        String roomKey = createRandomGameAndFillIt(ws, players);
        ws.listenToChannel("/topic/lobby/" + roomKey, FrameHandlerType.LOBBY_RESPONSE);

        GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(roomKey, players.get(0));
        ws.queues.get(FrameHandlerType.LOBBY_RESPONSE).clear();

        ws.sendToSocket("/app/leaveGame", request);
        await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(ws.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        ws.shutdown();
    }

    @Test
    void verifyThatLeaveGameDoesNotSendMessageIfPlayerWasNotInLobby() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        String roomKey = createRandomGameAndFillIt(ws);
        ws.listenToChannel("/topic/lobby/" + roomKey, FrameHandlerType.LOBBY_RESPONSE);

        GamePlayer player = GameControllerTestUtil.createValidGamePlayer();
        GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(roomKey, player);
        ws.queues.get(FrameHandlerType.LOBBY_RESPONSE).clear();

        ws.sendToSocket("/app/leaveGame", request);
        await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(ws.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        ws.shutdown();
    }

    @Test
    void verifyThatLeaveGameThrowsErrorWhenMissingRoomKey() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        String roomKey = createRandomGameAndFillIt(ws);
        ws.listenToChannel("/topic/lobby/" + roomKey, FrameHandlerType.LOBBY_RESPONSE);

        GamePlayer player = GameControllerTestUtil.createValidGamePlayer();
        GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(null, player);
        ws.queues.get(FrameHandlerType.LOBBY_RESPONSE).clear();

        ws.sendToSocket("/app/leaveGame", request);
        await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(ws.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(ws.isQueueEmpty(FrameHandlerType.ERROR_RESPONSE)));
        ws.shutdown();
    }

    @Test
    void verifyReadyReturnsMessageToLobby() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        List<GamePlayer> players = new ArrayList<>();
        String roomKey = createRandomGameAndFillIt(ws, players);

        ws.listenToChannel("/topic/lobby/" + roomKey, FrameHandlerType.LOBBY_RESPONSE);
        for(var player : players) {
            GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(roomKey, player);
            request.getPlayer().setReady(true);
            ws.sendToSocket("/app/ready", request);
            await().atMost(5, SECONDS).untilAsserted(() -> assertNotNull(ws.pollQueue(FrameHandlerType.LOBBY_RESPONSE)));
        }
        ws.shutdown();
    }

    // -- MARK: Testing the sync calls
    @Test
    void verifyThatFirstSyncCallReturnsStartResponse() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper wsTestHelper = new WsTestHelper(getWsPath());
        final List<GamePlayer> playersInGame = new ArrayList<>();
        String roomKey = createRandomGameAndFillIt(wsTestHelper, playersInGame);
        wsTestHelper.listenToChannel("/topic/game/startRound/" + roomKey, FrameHandlerType.START_ROUND_RESPONSE);

        int index, length = playersInGame.size();
        for(index = 0; index < length; index++) {
            GamePlayer player = playersInGame.get(index);
            GameInteractionRequest gameInteractionRequest = GameControllerTestUtil.createGameInteractionRequest(roomKey, player);
            wsTestHelper.sendToSocket("/app/game/sync", gameInteractionRequest);
        }

        await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.START_ROUND_RESPONSE)));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatSecondSyncCallReturnsNextTurnResponse() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        final List<GamePlayer> players = new ArrayList<>(4);
        String roomKey = createRandomGameAndFillIt(ws, players);

        StartRoundResponse startRoundResponse = (StartRoundResponse) doASyncCall(ws, players, roomKey, FrameHandlerType.START_ROUND_RESPONSE);
        assertNotNull(startRoundResponse);
        NextTurnResponse nextTurnResponse = (NextTurnResponse) doASyncCall(ws, players, roomKey, FrameHandlerType.NEXT_TURN_RESPONSE);
        assertNotNull(nextTurnResponse);
        ws.shutdown();
    }

    @Test
    void verifyThatEndpointsWorkAsExpectedWhenMockPlaying() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper ws = new WsTestHelper(getWsPath());
        final List<GamePlayer> players = new ArrayList<>(4);
        String roomKey = createRandomGameAndFillIt(ws, players);

        // Get the first start round response from the server
        StartRoundResponse startRoundResponse = (StartRoundResponse) doASyncCall(ws, players, roomKey, FrameHandlerType.START_ROUND_RESPONSE);
        GameHelper gameHelper = new GameHelper(startRoundResponse.getPlayersAndCards(), roomKey);

        // Listen to all the channels needed for the game
        ws.listenToChannel("/topic/game/nextTurn/" + roomKey, FrameHandlerType.NEXT_TURN_RESPONSE);
        ws.listenToChannel("/topic/game/roundStatus/" + roomKey, FrameHandlerType.ROUND_STATUS_RESPONSE);
        ws.listenToChannel("/topic/game/turnStatus/" + roomKey, FrameHandlerType.PLAYED_CARD_RESPONSE);

        // Game Loop!
        while(true) {
            ws.clearAllQueues();
            syncAllPlayers(ws, players, roomKey);

            await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(ws.isQueueEmpty(FrameHandlerType.NEXT_TURN_RESPONSE) && ws.isQueueEmpty(FrameHandlerType.ROUND_STATUS_RESPONSE)));

            // Handle the case where there was not next turn response and instead the round status response was sent
            // TODO: implement a full game loop here, not just one turn
            if(ws.isQueueEmpty(FrameHandlerType.NEXT_TURN_RESPONSE)) {
                assertFalse(ws.isQueueEmpty(FrameHandlerType.ROUND_STATUS_RESPONSE));
                RoundStatusResponse roundStatusResponse = (RoundStatusResponse) ws.pollQueue(FrameHandlerType.ROUND_STATUS_RESPONSE);
                System.out.println("ROUND STATUS RESPONSE OBJECT: " + roundStatusResponse);
                break;
            }

            // In this case, the next turn response queue is not empty
            assertFalse(ws.isQueueEmpty(FrameHandlerType.NEXT_TURN_RESPONSE));
            NextTurnResponse nextTurnResponse = (NextTurnResponse) ws.pollQueue(FrameHandlerType.NEXT_TURN_RESPONSE);
            gameHelper.addCard(nextTurnResponse);

            PlayCardRequestWrapper playCardRequest = gameHelper.playRandomCard();
            ws.sendToSocket("/app/game/playCard", playCardRequest);

            await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(ws.isQueueEmpty(FrameHandlerType.PLAYED_CARD_RESPONSE)));
            PlayedCardResponse playedCardResponse = (PlayedCardResponse) ws.pollQueue(FrameHandlerType.PLAYED_CARD_RESPONSE);
            gameHelper.playCard(playedCardResponse);
        }
        ws.shutdown();
     }

    private Object doASyncCall(WsTestHelper ws, List<GamePlayer> players, String key, FrameHandlerType responseExpected) throws InterruptedException {
        switch(responseExpected) {
            case START_ROUND_RESPONSE -> ws.listenToChannel("/topic/game/startRound/" + key, responseExpected);
            case NEXT_TURN_RESPONSE -> ws.listenToChannel("/topic/game/nextTurn/" + key, responseExpected);
            case ROUND_STATUS_RESPONSE -> ws.listenToChannel("/topic/game/roundStatus/" + key, responseExpected);
            default -> throw new RuntimeException("Unexpected frame handler passed in to sync call!");
        }

        for (var player : players) {
            GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(key, player);
            ws.sendToSocket("/app/game/sync", request);
        }

        await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(ws.isQueueEmpty(responseExpected)));
        return ws.pollQueue(responseExpected);
    }

    private void syncAllPlayers(WsTestHelper ws, List<GamePlayer> players, String key) {
        for (var player : players) {
            GameInteractionRequest request = GameControllerTestUtil.createGameInteractionRequest(key, player);
            ws.sendToSocket("/app/game/sync", request);
        }
    }

    private String createRandomGame(WsTestHelper wsTestHelper) throws InterruptedException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.RANDOM_PLAYER);
        UUID randomUUID = createGameRequest.getPlayer().getId();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
        LobbyResponse lobbyResponse = (LobbyResponse) wsTestHelper.pollQueue(FrameHandlerType.LOBBY_RESPONSE);
        return lobbyResponse.getRoomKey();
    }

    private String createRandomGameAndFillIt(WsTestHelper wsTestHelper) throws InterruptedException {
        String roomKey = createRandomGame(wsTestHelper);
        for(int i = 0; i < 3; i++) {
            JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.RANDOM_PLAYER);
            GamePlayer playerCreated = joinGameRequest.getPlayer();
            UUID randomUUID = playerCreated.getId();
            wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID, FrameHandlerType.LOBBY_RESPONSE);
            wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);
            await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
            wsTestHelper.pollQueue(FrameHandlerType.LOBBY_RESPONSE);
        }
        return roomKey;
    }

    private String createRandomGameAndFillIt(WsTestHelper wsTestHelper, List<GamePlayer> playerList) throws InterruptedException {
        UUID randomUUID;
        // Create the game
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.RANDOM_PLAYER);
        playerList.add(createGameRequest.getPlayer());
        randomUUID = createGameRequest.getPlayer().getId();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID, FrameHandlerType.LOBBY_RESPONSE);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));

        // Get room key
        String roomKey =  ((LobbyResponse)wsTestHelper.pollQueue(FrameHandlerType.LOBBY_RESPONSE)).getRoomKey();

        // Join the rest of the players and add to list
        for(int i = 0; i < 3; i++) {
            JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.RANDOM_PLAYER);
            GamePlayer playerCreated = joinGameRequest.getPlayer();
            randomUUID = playerCreated.getId();
            wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID, FrameHandlerType.LOBBY_RESPONSE);
            wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);
            await().atMost(5, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.isQueueEmpty(FrameHandlerType.LOBBY_RESPONSE)));
            wsTestHelper.pollQueue(FrameHandlerType.LOBBY_RESPONSE);
            playerList.add(playerCreated);
        }
        return roomKey;
    }

    private void startGame(WsTestHelper wsTestHelper, List<GamePlayer> players, String roomKey) {
        wsTestHelper.queues.get(FrameHandlerType.LOBBY_RESPONSE).clear();
        for(GamePlayer player : players) {
            GameInteractionRequest gameInteractionRequest = GameControllerTestUtil.createGameInteractionRequest(roomKey, player);
            wsTestHelper.sendToSocket("/app/game/sync", gameInteractionRequest);
        }
    }

    public class WsTestHelper {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Map<FrameHandlerType, BlockingQueue<?>> queues = new EnumMap<>(FrameHandlerType.class);
        private final Map<FrameHandlerType, StompFrameHandler> frameHandlers = new EnumMap<>(FrameHandlerType.class);
        private final StompSession session;

        public WsTestHelper(String path) throws ExecutionException, InterruptedException, TimeoutException {
            session = webSocketStompClient.connectAsync(path, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
            initializeQueuesAndFrameHandlers();
            session.subscribe("/topic/error/" + GameControllerTestUtil.TEST_PLAYER_UUID, frameHandlers.get(FrameHandlerType.ERROR_RESPONSE));
        }
        public void listenToChannel(String url, FrameHandlerType frameHandlerType) {
            session.subscribe(url, frameHandlers.get(frameHandlerType));
        }
        public void sendToSocket(String path, Object data) {
            session.send(path, data);
        }
        public boolean isQueueEmpty(FrameHandlerType forFrame) {
            return queues.get(forFrame).isEmpty();
        }
        public Object pollQueue(FrameHandlerType forFrame) throws InterruptedException {
            return queues.get(forFrame).poll(3, SECONDS);
        }
        public void clearAllQueues() {
            queues.values().forEach(BlockingQueue::clear);
        }
        public void shutdown() {
            session.disconnect();
            queues.values().forEach(BlockingQueue::clear);
        }
        private void initializeQueuesAndFrameHandlers() {
            for (FrameHandlerType type : FrameHandlerType.values()) {
                Class<?> clazz = type.getClassType();
                initializeQueueAndFrameHandler(clazz, type);
            }
        }
        private <T> void initializeQueueAndFrameHandler(Class<T> clazz, FrameHandlerType frameHandlerType) {
            if(frameHandlerType == FrameHandlerType.AVAILABLE_SERVER_RESPONSE) {
                initializeAvailableServerResponseQueueAndFrameHandler();
                return;
            }
            BlockingQueue<T> queue = new ArrayBlockingQueue<>(5);
            queues.put(frameHandlerType, queue);

            StompFrameHandler handler = new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return clazz;
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    T response = objectMapper.convertValue(payload, clazz);
                    boolean success = queue.offer(response);
                    if (!success) {
                        throw new RuntimeException("Unable to add to queue: " + queue);
                    }
                }
            };
            frameHandlers.put(frameHandlerType, handler);
        }
        private void initializeAvailableServerResponseQueueAndFrameHandler() {
            ArrayBlockingQueue<List<ServerData>> arrayBlockingQueue = new ArrayBlockingQueue<>(5);
            StompFrameHandler frameHandler = new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return new ParameterizedTypeReference<List<ServerData>>() {}.getType();
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    try {
                        List<ServerData> response = objectMapper.convertValue(payload, new TypeReference<List<ServerData>>() {});
                        boolean success = arrayBlockingQueue.offer(response);
                        if (!success) {
                            System.out.println("Unable to add response to the queue.");
                        } else {
                            System.out.println("Able to add the response to the queue.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error handling payload: " + e.getMessage());
                    }
                }
            };
            queues.put(FrameHandlerType.AVAILABLE_SERVER_RESPONSE, arrayBlockingQueue);
            frameHandlers.put(FrameHandlerType.AVAILABLE_SERVER_RESPONSE, frameHandler);
        }
    }

    private class GameHelper {

        private final Map<GamePlayer, List<Card>> players;
        private GamePlayer playerWhoseTurnItIs = null;
        private final Random random = new Random();
        private final String roomKey;

        public GameHelper(List<PlayerCardPair> startingData, String roomKey) {
            this.roomKey = roomKey;
            players = new HashMap<>();
            for(var pair : startingData) {
                players.put(pair.getPlayer(), new ArrayList<>());
                players.get(pair.getPlayer()).add(pair.getCard());
            }
        }
        public void playCard(PlayedCardResponse playedCardResponse) {
            System.out.println("Call to playCard with PlayedCardResponse: " + playedCardResponse);
            GamePlayer playerWhoPlayed = playedCardResponse.getPlayerWhoPlayed();
            List<Card> playersCards = players.get(playerWhoPlayed);
            Card playedCard = playedCardResponse.getCardPlayed();
            for(var card : playersCards) {
                if (card.getPower() == playedCard.getPower()) {
                    playersCards.remove(card);
                    break;
                }
            }

            // Do this to update the players boolean fields (idk if you can alter the key of a map)
            players.remove(playerWhoPlayed);
            players.put(playerWhoPlayed, playersCards);

            doCardLogic(playedCardResponse.getOutcome(), playerWhoPlayed);
        }
        private void doCardLogic(BaseCardResult result, GamePlayer playerWhoPlayed) {
            if(result instanceof DuckResult duckResult) {
                // Do duck result
                doDuckResultLogic(duckResult);
            } else if(result instanceof GazeboResult gazeboResult) {
                // Do gazebo result
                doGazeboResultLogic(gazeboResult, playerWhoPlayed);
            } else if(result instanceof MaulRatResult maulRatResult) {
                // Do maul rat result
                doMaulRatResultLogic(maulRatResult);
            } else if(result instanceof NetTrollResult netTrollResult) {
                // Do the net troll result
                doNetTrollResultLogic(netTrollResult);
            } else if(result instanceof PottedResult pottedResult) {
                // Do the potted result
                doPottedPlantResultLogic(pottedResult);
            } else {
                GamePlayer playerEffected = result.getPlayedOn();
                List<Card> playerEffectedCards = players.get(playerEffected);
                if(playerEffectedCards == null) {
                    throw new RuntimeException("Unable to get the effected player from the map!");
                }

                // Refresh the mapping in case their status variables changed
                players.remove(playerEffected);
                players.put(playerEffected, playerEffectedCards);
            }
        }
        private void doDuckResultLogic(DuckResult duckResult) {
            GamePlayer playerWhoLost = duckResult.getPlayerToDiscard();
            if(playerWhoLost.getIsOut() != Boolean.TRUE) {
                throw new RuntimeException("The player who lost the duck of doom is not marked as out!");
            }

            List<Card> playerWhoLostCards = players.get(playerWhoLost);
            if(playerWhoLostCards == null) {
                throw new RuntimeException("Could not find the player who lost in map! (DUCK OF DOOM)");
            }

            // Refresh mapping
            assert playerWhoLost.getIsOut();
            playerWhoLostCards.clear();
            players.remove(playerWhoLost);
            players.put(playerWhoLost, playerWhoLostCards);
        }
        private void doGazeboResultLogic(GazeboResult gazeboResult, GamePlayer playerWhoPlayed) {
            GamePlayer opponent = gazeboResult.getPlayedOn();
            Card playerWhoPlayedCard = gazeboResult.getPlayersCard();
            Card opponentCard = gazeboResult.getOpponentCard();

            List<Card> playerWhoPlayedCards = players.get(playerWhoPlayed);
            playerWhoPlayedCards.remove(playerWhoPlayedCard);
            playerWhoPlayedCards.add(opponentCard);

            List<Card> opponentCards = players.get(opponent);
            opponentCards.remove(opponentCard);
            opponentCards.add(playerWhoPlayedCard);
        }
        private void doMaulRatResultLogic(MaulRatResult maulRatResult) {
            // nothing happens
        }
        private void doNetTrollResultLogic(NetTrollResult netTrollResult) {
            GamePlayer playedOn = netTrollResult.getPlayedOn();
            Card discardedCard = netTrollResult.getDiscardedCard();

            List<Card> playedOnCards = players.get(playedOn);
            if(playedOnCards == null) {
                throw new RuntimeException("Unable to retrieve the played on player from the map! (NET TROLL RESULT)");
            }
            playedOnCards.remove(discardedCard);

            if(playedOn.getIsOut()) {
                players.remove(playedOn);
                players.put(playedOn, playedOnCards);
                return;
            }

            Card drawnCard = netTrollResult.getDrawnCard();
            playedOnCards.add(drawnCard);
            players.put(playedOn, playedOnCards);
        }
        private void doPottedPlantResultLogic(PottedResult pottedResult) {
            GamePlayer playedOn = pottedResult.getPlayedOn();
            List<Card> playedOnCards = players.get(playedOn);

            if(playedOnCards == null) {
                throw new RuntimeException("Unable to get played on player from the map! (POTTED RESULT)");
            }

            Card cardGuessed = pottedResult.getGuessedCard();
            boolean correctGuess = pottedResult.getCorrectGuess();

            if(correctGuess) {
                assert playedOn.getIsOut();
                playedOnCards.remove(cardGuessed);
                players.remove(playedOn);
                players.put(playedOn, playedOnCards);
            }
        }
        public void addCard(NextTurnResponse nextTurnResponse) {
            System.out.println("Call to addCard, nextTurnResponse = " + nextTurnResponse);
            if(nextTurnResponse == null) {
                throw new RuntimeException("NULL NextTurnResponse detected in addCard!");
            }

            GamePlayer player = nextTurnResponse.getPlayer();
            Card card = nextTurnResponse.getCard();

            List<Card> mapping = players.get(player);
            if(mapping == null) {
                throw new RuntimeException("NULL list of cards in addCard for player -> " + player);
            }

            mapping.add(card);
            players.put(player, mapping);
            playerWhoseTurnItIs = player;
        }
        public PlayCardRequestWrapper playRandomCard() {
            if(playerWhoseTurnItIs == null) {
                throw new RuntimeException("The current player is null! Something went wrong.");
            }

            System.out.println("Call to playRandomCard: Player whose turn it is: " + playerWhoseTurnItIs);

            List<Card> playersCards = players.get(playerWhoseTurnItIs);
            int indexOfCardToPlay = random.nextInt() % 2 == 0 ? 0 : 1;
            Card cardToBePlayed = playersCards.remove(indexOfCardToPlay);
            PlayedCardWrapper playedCard = playThisCardRandomly(cardToBePlayed, playerWhoseTurnItIs);
            return PlayCardRequestWrapper.builder()
                    .roomKey(roomKey)
                    .player(playerWhoseTurnItIs)
                    .card(playedCard)
                    .build();
        }
        private PlayedCardWrapper playThisCardRandomly(Card card, GamePlayer self) {
            return switch (card.getPower()) {
                case 1:
                    GuessingCard guessingCard = new GuessingCard(1, random.nextInt(2, 9), getRandomPlayer(self));
                    yield new PlayedCardWrapper(guessingCard);
                case 2, 3, 5, 6:
                    TargetedEffectCard targetedEffectCard = new TargetedEffectCard(card.getPower(), getRandomPlayer(self));
                    yield new PlayedCardWrapper(targetedEffectCard);
                case 4, 7, 8:
                    PlayedCard playedCard = new PlayedCard(card.getPower());
                    yield new PlayedCardWrapper(playedCard);
                default:
                    throw new RuntimeException("Unknown card level passed in to playThisCardRandomly -> " + card.getPower());
            };
        }
        private GamePlayer getRandomPlayer(GamePlayer self) {
            for(var player : players.keySet()) {
                if(player.equals(self)) continue;
                if(!player.getIsOut()) return player;
            }
            throw new RuntimeException("There are no players in the round that are not out!");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PlayedCardWrapper {

        @JsonProperty
        private int power;
        @JsonProperty
        private String type;
        @JsonProperty
        private GamePlayer guessedOn;
        @JsonProperty
        private int guessedCard;
        @JsonProperty
        private GamePlayer playedOn;

        @JsonIgnore
        public PlayedCardWrapper(PlayedCard playedCard) {
            this.type = "personal";
            this.power = playedCard.getPower();
        }

        @JsonIgnore
        public PlayedCardWrapper(GuessingCard guessingCard) {
            this.type = "guess";
            this.power = guessingCard.getPower();
            this.guessedOn = guessingCard.getGuessedOn();
            this.guessedCard = guessingCard.getGuessedCard();
        }

        @JsonIgnore
        public PlayedCardWrapper(TargetedEffectCard targetedEffectCard) {
            this.type = "targeted";
            this.power = targetedEffectCard.getPower();
            this.playedOn = targetedEffectCard.getPlayedOn();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PlayCardRequestWrapper {

        @JsonProperty
        private String roomKey;

        @JsonProperty
        private GamePlayer player;

        @JsonProperty
        private PlayedCardWrapper card;
    }
}
