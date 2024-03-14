package com.loot.server;

import ch.qos.logback.core.pattern.color.ANSIConstants;
import com.loot.server.service.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class SessionEventListener {

    @Autowired
    private SessionCacheService sessionCacheService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());

        var simpSessionId = stompHeaderAccessor.getSessionId();
        System.out.println("Received a session connected event for new client with session id "+simpSessionId+"...");
        sessionCacheService.newConnection(simpSessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String simpSessionId = event.getSessionId();
        sessionCacheService.markClientConnection(simpSessionId);

        System.out.println(ANSIConstants.RED_FG + "A client session with id(" + simpSessionId + ") has been marked." + ANSIConstants.RESET);
    }
}
