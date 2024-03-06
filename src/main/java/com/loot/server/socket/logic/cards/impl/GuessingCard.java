package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.GamePlayer;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuessingCard extends PlayedCard {

    @JsonIgnore
    public GuessingCard(int playedCard, int guessedCard, GamePlayer guessedOn) {
        super(playedCard);
        this.guessedCard = guessedCard;
        this.guessedOn = guessedOn;
    }

    @JsonProperty
    private GamePlayer guessedOn;

    @JsonProperty
    private int guessedCard;

    @Override
    public String toString() {
        return "Guessing Card:\n\tpower: " + this.getPower() + "\n\tguessedOn: " + this.getGuessedOn() + "\n\tguessedCard: " + this.getGuessedCard();
    }
}
