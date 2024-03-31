package com.loot.server.service.impl;

import java.util.*;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.PlayedCard;
import com.loot.server.domain.request.*;
import com.loot.server.domain.response.*;
import com.loot.server.ClientDisconnectionEvent;
import com.loot.server.service.GameControllerService;
import com.loot.server.service.SessionCacheService;
import com.loot.server.logic.impl.GameSession;
import com.loot.server.logic.impl.GameSession.GameAction;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameControllerServiceImpl implements GameControllerService {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Autowired
    private SessionCacheService sessionCacheService;

    @Autowired // not sure about keeping this in here, but for now it stays
    private SimpMessagingTemplate simpMessagingTemplate;

    private final Set<String> inUseRoomKeys = new HashSet<>();
    private final Map<String, GameSession> gameSessionMap = new HashMap<>();

    synchronized private void addToGameSessionMap(String key, GameSession gameSession) {
        gameSessionMap.put(key, gameSession);
    }

    synchronized private GameSession getFromGameSessionMap(String key) {
        return gameSessionMap.get(key);
    }

    synchronized private List<GameSession> getAllGameSessions() {
        return List.copyOf(gameSessionMap.values());
    }

    synchronized private void removeFromGameSessionMap(String key) {
        gameSessionMap.remove(key);
    }

    @Override
    public String createNewGameSession(CreateGameRequest request, String sessionId) {
        String roomKey = getRoomKeyForNewGame();
        String roomName = request.getRoomName();
        var player = request.getPlayer();

        addToGameSessionMap(roomKey, new GameSession(roomKey, roomName));
        var gameSession = getFromGameSessionMap(roomKey);
        gameSession.addPlayer(player);

        sessionCacheService.cacheClientConnection(player.getId(), roomKey, sessionId);
        return roomKey;
    }

    @Override
    public void joinCurrentGameSession(JoinGameRequest request, String sessionId) {
        String roomKey = request.getRoomKey();
        GameSession gameSession = getFromGameSessionMap(roomKey);
        GamePlayer player = request.getPlayer();
        gameSession.addPlayer(player);

        sessionCacheService.cacheClientConnection(player.getId(), roomKey, sessionId);
    }

    @Override
    public Boolean gameAbleToBeJoined(String roomKey) {
        GameSession gameSession = getFromGameSessionMap(roomKey);
        if(gameSession == null) {
            return Boolean.FALSE;
        }

        if(gameSession.getNumberOfPlayers() == gameSession.getMaxPlayers()) {
            return Boolean.FALSE;
        }
        if(gameSession.isGameInProgress()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean gameExists(String roomKey) {
        GameSession gameSession = getFromGameSessionMap(roomKey);
        return gameSession != null;
    }

    @Override
    public void changePlayerReadyStatus(GameInteractionRequest request) {
        String roomKey = request.getRoomKey();
        var player = request.getPlayer();
        var gameSession = getFromGameSessionMap(roomKey);
        gameSession.changePlayerReadyStatus(player);
    }

    @Override
    public GameAction syncPlayer(String roomKey, GamePlayer player) {
        GameSession gameSession = getFromGameSessionMap(roomKey);
        return gameSession.syncPlayer(player);
    }

    @Override
    public StartRoundResponse startRound(String roomKey) {
        GameSession gameSession = getFromGameSessionMap(roomKey);
        Pair<List<GamePlayer>, List<Card>> startingInformation = gameSession.startRound();
        return new StartRoundResponse(startingInformation.getLeft(), startingInformation.getRight());
    }

    @Override
    public RoundStatusResponse getRoundStatus(String roomKey) {
        GameSession gameSession = getFromGameSessionMap(roomKey);
        return gameSession.determineWinner();
    }

    @Override
    public PlayedCardResponse playCard(PlayCardRequest playCardRequest) {
        GamePlayer player = playCardRequest.getPlayer();
        PlayedCard card = playCardRequest.getCard();
        GameSession gameSession = getFromGameSessionMap(playCardRequest.getRoomKey());

        return gameSession.playCard(player, card);
    }

    @Override
    public NextTurnResponse getNextTurn(String roomKey) {
        GameSession gameSession = getFromGameSessionMap(roomKey);

        GamePlayer nextPlayer = gameSession.nextTurn();
        Card dealtCard = gameSession.dealCard(nextPlayer);
        return NextTurnResponse.builder()
                .player(nextPlayer)
                .card(dealtCard)
                .build();
    }

    @Override
    public void removePlayerFromGameSession(GameInteractionRequest gameInteractionRequest, String sessionId) {
        var player = gameInteractionRequest.getPlayer();
        var gameSession = getFromGameSessionMap(gameInteractionRequest.getRoomKey());
        gameSession.removePlayer(player);
        sessionCacheService.uncacheClientConnection(sessionId);
        validateGameSession(gameSession);
    }

    @Override
    public LobbyResponse getInformationForLobby(String roomKey) {
        var gameSession = getFromGameSessionMap(roomKey);
        if(gameSession == null) {
            return null;
        }

        return LobbyResponse.builder()
                .roomKey(roomKey)
                .players(gameSession.getPlayers())
                .allReady(gameSession.allPlayersReady())
                .build();
    }

    @EventListener
    public void clientDisconnectedCallback(ClientDisconnectionEvent clientDisconnectionEvent) {
        UUID clientUUID = clientDisconnectionEvent.getClientUUID();
        String clientRoomKey = clientDisconnectionEvent.getGameRoomKey();

        var gameSession = getFromGameSessionMap(clientRoomKey);
        if(gameSession == null) {
            return;
        }

        for(var player: gameSession.getPlayers()) {
            if(player.getId().equals(clientUUID)) {
                gameSession.removePlayer(player);
                updateLobbyOnDisconnect(gameSession);
                break;
            }
        }
        validateGameSession(gameSession);
    }

    @Override
    public void updateLobbyOnDisconnect(GameSession gameSession) {
        String roomKey = gameSession.getRoomKey();
        LobbyResponse lobbyResponse = getInformationForLobby(roomKey);
        simpMessagingTemplate.convertAndSend("/topic/lobby/" + roomKey, lobbyResponse);
    }

    @Override
    public void validateGameSession(GameSession gameSession) {
        if (gameSession.getPlayers().isEmpty()) {
            String roomKey = gameSession.getRoomKey();
            removeFromGameSessionMap(roomKey);
        }
    }

    @Override
    public String generateRoomKey() {
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        final int keyLength = 5;
        final int length = allowedCharacters.length() - 1;

        StringBuilder key = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i < keyLength; ++i) {
            key.append(allowedCharacters.charAt(rand.nextInt(length)));
        }

        return key.toString();
    }

    @Override
    public String getRoomKeyForNewGame() {
        String roomKey;
        do {
            roomKey = generateRoomKey();
        } while(inUseRoomKeys.contains(roomKey));
        inUseRoomKeys.add(roomKey);
        return roomKey;
    }

    @Override
    public List<ServerData> getListOfServers() {
        final List<ServerData> currentServers = new ArrayList<>();
        getAllGameSessions().forEach(gameSession -> {
            String status;
            int maxPlayers = gameSession.getMaxPlayers(), actualPlayers = gameSession.getNumberOfPlayers();
            if(gameSession.isGameInProgress()) {
                status = "In Progress";
            } else if(actualPlayers >= maxPlayers) {
                status = "Full";
            } else {
                status = "Available";
            }
            currentServers.add(
                    ServerData.builder()
                            .name(gameSession.getName())
                            .key(gameSession.getRoomKey())
                            .maximumPlayers(maxPlayers)
                            .numberOfPlayers(actualPlayers)
                            .status(status)
                            .build()
            );
        });
        return currentServers;
    }
}
