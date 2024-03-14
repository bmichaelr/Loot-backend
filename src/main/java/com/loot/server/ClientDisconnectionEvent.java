package com.loot.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class ClientDisconnectionEvent extends ApplicationEvent {

    @Getter
    @Setter
    private String clientName;

    @Getter
    @Setter
    private String gameRoomKey;

    public ClientDisconnectionEvent(Object source, String clientName, String gameRoomKey) {
        super(source);
        this.clientName = clientName;
        this.gameRoomKey = gameRoomKey;
    }
}
