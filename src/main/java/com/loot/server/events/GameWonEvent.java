package com.loot.server.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Setter
public class GameWonEvent extends ApplicationEvent {

    private UUID playerId;

    public GameWonEvent(Object source, UUID playerId) {
        super(source);
        this.playerId = playerId;
    }
}
