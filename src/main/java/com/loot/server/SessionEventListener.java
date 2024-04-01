package com.loot.server;

import com.loot.server.service.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class SessionEventListener {

    @Autowired
    private SessionCacheService sessionCacheService;

    private boolean applicationShuttingDown = false; // Debug variable

    @EventListener
    public void onApplicationEvent(ContextClosedEvent closedEvent) {
        applicationShuttingDown = true;

        System.out.println("Application is shutting down. Performing cleanup and terminating channels...");
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var simpSessionId = stompHeaderAccessor.getSessionId();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if(applicationShuttingDown) {
            return;
        }

        String simpSessionId = event.getSessionId();
        sessionCacheService.uncacheClientConnection(simpSessionId);
    }
}
