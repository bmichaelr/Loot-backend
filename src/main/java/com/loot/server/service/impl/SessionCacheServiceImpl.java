package com.loot.server.service.impl;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import com.loot.server.service.SessionCacheService;
import com.loot.server.socket.GameController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionCacheServiceImpl implements SessionCacheService {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    static {
        sessionCache = new HashMap<>();
    }

    private final static Map<String, Tuple<String, String>> sessionCache;
    private final List<Tuple<String, LocalDateTime>> markedSessions = new ArrayList<>();


    @Override
    public void cacheClientConnection(String clientName, String gameRoomKey, String simpSessionId) {
        sessionCache.put(simpSessionId, Tuple.of(clientName, gameRoomKey));
    }

    @Override
    public void markClientConnection(String simpSessionId) {
        if(!sessionCache.containsKey(simpSessionId)) {
            return;
        }

        markedSessions.add(Tuple.of(simpSessionId, LocalDateTime.now()));
    }

    @Override
    public Boolean clientConnectionExists(String simpSessionId) {
        return sessionCache.containsKey(simpSessionId);
    }

    @Scheduled(cron = "*/30 * * * * *")
    private void killExpiredSessions() {
        System.out.println(ANSI_RED + "Running session kill..." + ANSI_RESET);

        LocalDateTime currentTime = LocalDateTime.now();

        markedSessions.forEach(session -> {
            var markedTime = session.value;
            var lapsedTime = Duration.between(markedTime, currentTime);
            var minutesDiff = Math.abs(lapsedTime.toMinutes());
            if(minutesDiff > 1) {
                System.out.println(ANSI_RED + "Found old session (" + session + "), killing..." + ANSI_RESET);
                var tuple = sessionCache.get(session.key);
                GameController.markedSessionCallback(tuple.key, tuple.value);
                markedSessions.remove(session);
            }
        });

        System.out.println(ANSI_RED + "Finished running session kill function..." + ANSI_RESET);
    }

    private static class Tuple<T, S> {
        T key;
        S value;

        public Tuple(T key, S value) {
            this.key = key;
            this.value = value;
        }

        public static <T, S> Tuple<T, S> of(T key, S value) {
            return new Tuple<>(key, value);
        }

        @Override
        public boolean equals(Object object) {
            if(object == null || this.getClass() != object.getClass()) {
                return false;
            }

            var tuple = (Tuple<?, ?>) object;
            return this.key.equals(tuple.key);
        }
    }
}
