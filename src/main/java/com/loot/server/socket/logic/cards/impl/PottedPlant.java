package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.GamePlayer;
import com.loot.server.socket.logic.cards.Card;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PottedPlant extends Card {

    @JsonProperty
    private GamePlayer playedOn;

    @JsonProperty
    private int guess;

    public PottedPlant(int power, String name, String description){
        super(power, name, description);
    }

    public PottedPlant(int power, String name, String description, GamePlayer playedOn, int guess){
        super(power, name, description);
        this.playedOn = playedOn;
        this.guess = guess;
    }

    @JsonIgnore
    public static PottedPlant createCard(){
        int power = 1;
        String name = "Potted Plant";
        String description = "Name a non-Potted Plant card and choose another player. If that player has that card, "
                                + "he or she is out of the round.";
        return new PottedPlant(power, name, description);
    }
}
