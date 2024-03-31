package com.loot.server.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.ErrorResponse;
import com.loot.server.domain.response.LobbyResponse;
import com.loot.server.ControllerTest.GameControllerTestUtil.RequestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {

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

    // -- MARK: Create Game Tests
    @Test
    void verifyCreateGameMessageIsReceivedWhenValid() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.VALID);

        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueNotEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueIsEmpty()));

        LobbyResponse lobbyResponse = wsTestHelper.successBlockingQueue.poll(2, SECONDS);
        System.out.println(lobbyResponse);
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenMissingRoomName() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.MISSING_ROOM_NAME);

        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenMissingPlayer() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.MISSING_GAME_PLAYER);

        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueIsEmpty()));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenGamePlayerMissingName() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.GAME_PLAYER_MISSING_NAME);

        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        wsTestHelper.shutdown();
    }

    @Test
    void verifyThatCreateGameFailsWhenGamePlayerMissingId() throws ExecutionException, InterruptedException, TimeoutException {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.GAME_PLAYER_MISSING_ID);

        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);


        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueIsEmpty()));
        wsTestHelper.shutdown();
    }

    // -- MARK: Join Game Tests
    @Test
    void verifyJoinGameMessageIsReceivedWhenValid() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueNotEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueIsEmpty()));
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameMSendsErrorWhenMissingRoomKey() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.MISSING_ROOM_KEY);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> {
            assertEquals("Bad request. Please try again.", wsTestHelper.pollErrorQueue().details);
        });
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameFailsWhenMissingPlayer() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.MISSING_GAME_PLAYER);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueIsEmpty()));
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameSendsErrorWhenWrongRoomKey() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        String roomKey = createRandomGame(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.WRONG_ROOM_KEY);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        assertEquals("The room could not be found, meaning it is likely no longer available. Please refresh the server list.", wsTestHelper.pollErrorQueue().details);
        wsTestHelper.shutdown();
    }
    @Test
    void verifyJoinGameSendsErrorWhenLobbyFull() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        String roomKey = createRandomGameAndFillIt(wsTestHelper);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueIsEmpty()));
        await().atMost(1, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertEquals("Cannot join game. It may be full or in progress, refresh server list to get updated status.",
                        wsTestHelper.pollErrorQueue().details)
        );
        wsTestHelper.shutdown();
    }

    @Test
    void verifyJoinGameSendsErrorWhenGameInProgress() throws ExecutionException, InterruptedException, TimeoutException {
        WsTestHelper<LobbyResponse> wsTestHelper = new WsTestHelper<>(LobbyResponse.class, getWsPath());
        wsTestHelper.withErrorHandling();

        final List<GamePlayer> playersInGame = new ArrayList<>();
        String roomKey = createRandomGameAndFillIt(wsTestHelper, playersInGame);
        startGame(wsTestHelper, playersInGame, roomKey);
        JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.VALID);
        wsTestHelper.listenToChannel("/topic/matchmaking/" + GameControllerTestUtil.TEST_PLAYER_UUID);
        wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);

        await().atMost(2, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.errorQueueNotEmpty()));
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertEquals("Cannot join game. It may be full or in progress, refresh server list to get updated status.",
                        wsTestHelper.pollErrorQueue().details)
        );

        wsTestHelper.printSuccessQueue();
        wsTestHelper.shutdown();
    }

    private String createRandomGame(WsTestHelper<?> wsTestHelper) {
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.RANDOM_PLAYER);
        UUID randomUUID = createGameRequest.getPlayer().getId();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);

        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.successQueueIsEmpty()));
        LobbyResponse lobbyResponse = (LobbyResponse) wsTestHelper.pollSuccessQueue();
        return lobbyResponse.getRoomKey();
    }

    private String createRandomGameAndFillIt(WsTestHelper<?> wsTestHelper) {
        String roomKey = createRandomGame(wsTestHelper);
        for(int i = 0; i < 3; i++) {
            JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.RANDOM_PLAYER);
            GamePlayer playerCreated = joinGameRequest.getPlayer();
            UUID randomUUID = playerCreated.getId();
            wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID);
            wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);
            await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueNotEmpty()));
            wsTestHelper.pollSuccessQueue();
        }
        return roomKey;
    }

    private String createRandomGameAndFillIt(WsTestHelper<?> wsTestHelper, List<GamePlayer> playerList) {
        UUID randomUUID;
        // Create the game
        CreateGameRequest createGameRequest = GameControllerTestUtil.createGameRequest(RequestType.RANDOM_PLAYER);
        playerList.add(createGameRequest.getPlayer());
        randomUUID = createGameRequest.getPlayer().getId();
        wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID);
        wsTestHelper.sendToSocket("/app/createGame", createGameRequest);
        await().atMost(1, SECONDS).untilAsserted(() -> assertFalse(wsTestHelper.successQueueIsEmpty()));

        // Get room key
        String roomKey =  ((LobbyResponse)wsTestHelper.pollSuccessQueue()).getRoomKey();

        // Join the rest of the players and add to list
        for(int i = 0; i < 3; i++) {
            JoinGameRequest joinGameRequest = GameControllerTestUtil.createJoinGameRequest(roomKey, RequestType.RANDOM_PLAYER);
            GamePlayer playerCreated = joinGameRequest.getPlayer();
            randomUUID = playerCreated.getId();
            wsTestHelper.listenToChannel("/topic/matchmaking/" + randomUUID);
            wsTestHelper.sendToSocket("/app/joinGame", joinGameRequest);
            await().atMost(5, SECONDS).untilAsserted(() -> assertTrue(wsTestHelper.successQueueNotEmpty()));
            wsTestHelper.pollSuccessQueue();
            playerList.add(playerCreated);
        }
        return roomKey;
    }

    private void startGame(WsTestHelper<?> wsTestHelper, List<GamePlayer> players, String roomKey) {
        wsTestHelper.clearSuccessQueue();
        //wsTestHelper.listenToChannel("/topic/game/startRound/" + roomKey);

        int index = 0;
        int length = players.size() - 1;
        for(GamePlayer player : players) {
            GameInteractionRequest gameInteractionRequest = GameControllerTestUtil.createGameInteractionRequest(roomKey, player);
            wsTestHelper.sendToSocket("/app/game/sync", gameInteractionRequest);
        }
    }

    private class WsTestHelper<T> {
        ObjectMapper objectMapper = new ObjectMapper();
        StompFrameHandler successHandler;
        StompFrameHandler errorHandler;
        public BlockingQueue<T> successBlockingQueue;
        public BlockingQueue<ErrorResponse> errorBlockingQueue;
        public StompSession session;

        public WsTestHelper(Class<T> responseType, String path) throws ExecutionException, InterruptedException, TimeoutException {
            successBlockingQueue = new ArrayBlockingQueue<>(5);
            session = webSocketStompClient
                    .connectAsync(path, new StompSessionHandlerAdapter() {})
                    .get(1, SECONDS);
            successHandler = new StompFrameHandler() {
                 @Override
                 @NonNull
                 public Type getPayloadType(@NonNull StompHeaders headers) {
                     return responseType;
                 }

                 @Override
                 public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    T response = objectMapper.convertValue(payload, responseType);
                    boolean success = successBlockingQueue.offer(response);
                    if(!success) {
                        throw new RuntimeException("Unable to add to success queue!");
                    }
                 }
            };
        }
        public void withErrorHandling() {
            errorBlockingQueue = new ArrayBlockingQueue<>(5);
            errorHandler = new StompFrameHandler() {
                @Override
                @NonNull
                public Type getPayloadType(@NonNull StompHeaders headers) {
                    return ErrorResponse.class;
                }

                @Override
                public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                    ErrorResponse response = objectMapper.convertValue(payload, ErrorResponse.class);
                    boolean success = errorBlockingQueue.offer(response);
                    if(!success) {
                        throw new RuntimeException("Unable to add to the error queue!");
                    }
                }
            };
            session.subscribe("/topic/error/" + GameControllerTestUtil.TEST_PLAYER_UUID, errorHandler);
        }
        public void listenToChannel(String url) {
            session.subscribe(url, successHandler);
        }
        public void sendToSocket(String path, Object data) {
            session.send(path, data);
        }
        public boolean successQueueIsEmpty() {
            return successBlockingQueue.isEmpty();
        }
        public boolean successQueueNotEmpty() {
            return !successQueueIsEmpty();
        }
        public boolean errorQueueIsEmpty() {
            return errorBlockingQueue.isEmpty();
        }
        public boolean errorQueueNotEmpty() {
            return !errorQueueIsEmpty();
        }
        public T pollSuccessQueue() {
            return successBlockingQueue.poll();
        }
        public void clearSuccessQueue() {
            successBlockingQueue.clear();
        }
        public ErrorResponse pollErrorQueue() {
            System.out.println("Polling Error Queue: " + errorBlockingQueue.peek());
            return errorBlockingQueue.poll();
        }
        public void printSuccessQueue() {
            System.out.println("Printing queue contents of size: " + successBlockingQueue.size());

            for (T t : successBlockingQueue) {
                System.out.println("Queue contents: " + t);
            }
        }
        public void shutdown() {
            session.disconnect();
            errorBlockingQueue.clear();
            successBlockingQueue.clear();
        }
    }
}
