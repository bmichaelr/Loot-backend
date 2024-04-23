package com.loot.server.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class PlayerControllerException extends Exception {
    Optional<UUID> playerID;
    public PlayerControllerException(String message) {
        super(message);
        playerID = Optional.empty();
    }
    public PlayerControllerException(String message, UUID playerID) {
        super(message);
        this.playerID = Optional.of(playerID);
    }
    public static PlayerControllerException badRequest(UUID uuid) {
        return new PlayerControllerException("ERROR : missing required information in request!", uuid);
    }
    public static PlayerControllerException notFound(UUID uuid) {
        return new PlayerControllerException("Unable to find player from given ID, please try again.", uuid);
    }
    public static PlayerControllerException nameTaken(UUID uuid) {
        return new PlayerControllerException("Unique name already taken.", uuid);
    }
}
