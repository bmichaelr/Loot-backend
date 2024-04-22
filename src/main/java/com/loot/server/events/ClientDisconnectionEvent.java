package com.loot.server.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Setter
public class ClientDisconnectionEvent extends ApplicationEvent {

    private UUID clientUUID;
    private String gameRoomKey;

    public ClientDisconnectionEvent(Object source, UUID clientUUID, String gameRoomKey) {
        super(source);
        this.clientUUID = clientUUID;
        this.gameRoomKey = gameRoomKey;
    }
}
