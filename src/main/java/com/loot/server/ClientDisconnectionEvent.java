package com.loot.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ClientDisconnectionEvent extends ApplicationEvent {

    @Getter
    @Setter
    private UUID clientUUID;

    @Getter
    @Setter
    private String gameRoomKey;

    public ClientDisconnectionEvent(Object source, UUID clientUUID, String gameRoomKey) {
        super(source);
        this.clientUUID = clientUUID;
        this.gameRoomKey = gameRoomKey;
    }
}
