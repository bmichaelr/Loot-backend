package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuessingCard extends PlayedCard {

    @JsonProperty
    private Long guessedPlayerId;

    @JsonProperty
    private int guessedCard;

    @Override
    public String toString() {
        return "Guessing Card:\n\tpower: " + this.getPower() + "\n\tguessedPlayerId: " + this.getGuessedPlayerId() + "\n\tguessedCard: " + this.getGuessedCard();
    }
}
