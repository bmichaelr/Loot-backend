package com.loot.server.service.impl;

import com.loot.server.ClientDisconnectionEvent;
import com.loot.server.service.SessionCacheService;
import lombok.Data;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionCacheServiceImpl implements SessionCacheService {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    static {
        sessionCache = new ConcurrentHashMap<>();
    }

    private final static ConcurrentHashMap<String, ConcurrentPair<UUID, String>> sessionCache;
    private final ApplicationEventPublisher eventPublisher;

    public SessionCacheServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void cacheClientConnection(UUID clientUUID, String gameRoomKey, String simpSessionId) {
        System.out.println(ANSI_RED + "Caching client connection..." + ANSI_RESET);
        sessionCache.put(simpSessionId, ConcurrentPair.of(clientUUID, gameRoomKey));

        memDump();
    }

    @Override
    public void uncacheClientConnection(String simpSessionId) {
        if(!sessionCache.containsKey(simpSessionId)) { return; }

        var clientInformation = sessionCache.remove(simpSessionId);
        if(clientInformation != null) {
            System.out.println(ANSI_RED + "Removed client connection => " + clientInformation + ANSI_RESET);
            eventPublisher.publishEvent(new ClientDisconnectionEvent(this, clientInformation.getKey(), clientInformation.getValue()));
        }

        memDump();
    }

    private void memDump() {
        System.out.print("Session Cache: {");
        for(var key : sessionCache.keySet()) {
            System.out.print("[" + key + ", " + sessionCache.get(key).toString()+ "], ");
        }
        System.out.println("}");
    }

    @Data
    private static class ConcurrentPair<T, S> {
        volatile private T key;
        volatile private S value;

        public ConcurrentPair(T key, S value) {
            this.key = key;
            this.value = value;
        }

        public static <T, S> ConcurrentPair<T, S> of(T key, S value) {
            return new ConcurrentPair<>(key, value);
        }

        @Override
        synchronized public boolean equals(Object object) {
            if(object == null || this.getClass() != object.getClass()) {
                return false;
            }

            var tuple = (ConcurrentPair<?, ?>) object;
            return this.getKey().equals(tuple.getKey());
        }

        @Override
        public String toString() {
            return "[key = " + key.toString() + ", value = " + value.toString() + "]";
        }
    }

//    private static class SessionMonitoringThread extends Thread {
//        private final ConcurrentLinkedQueue<ConcurrentPair<String, LocalDateTime>> markedSessions;
//        private final ApplicationEventPublisher applicationEventPublisher;
//        private boolean running;
//
//        public SessionMonitoringThread(ConcurrentLinkedQueue<ConcurrentPair<String, LocalDateTime>> markedSessions, ApplicationEventPublisher applicationEventPublisher) {
//            this.markedSessions = markedSessions;
//            this.applicationEventPublisher = applicationEventPublisher;
//            running = false;
//        }
//
//        @Override
//        public void run() {
//            while(running) {
//                if(!markedSessions.isEmpty()) {
//                    System.out.println(ANSI_RED + "Checking the queue for old sessions..." + ANSI_RESET);
//                    var currentTime = LocalDateTime.now();
//                    var iterator = markedSessions.iterator();
//                    while(iterator.hasNext()) {
//                        var pair = iterator.next();
//                        var oldTime = pair.getValue();
//                        long secondsElapsed = Duration.between(currentTime, oldTime).toSeconds();
//                        if(secondsElapsed >= 30) {
//                            System.out.println("Found old session => " + pair);
//                            applicationEventPublisher.publishEvent(new ClientDisconnectionEvent(this, ));
//                            iterator.remove();
//                        }
//                    }
//                } else {
//                    stopThread();
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//
//        synchronized public void stopThread() {
//            this.running = false;
//        }
//    }
}
