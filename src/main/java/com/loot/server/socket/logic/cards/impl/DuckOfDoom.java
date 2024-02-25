package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.socket.logic.cards.BaseCard;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuckOfDoom extends BaseCard {

    @JsonProperty
    private String playedOn;

    public DuckOfDoom(int power, String name, String description) {
        super(power, name, description);
    }

    public DuckOfDoom(int power, String name, String description, String playedOn) {
        super(power, name, description);
        this.playedOn = playedOn;
    }

    @JsonIgnore
    public static DuckOfDoom createCard(){
        int power = 3;
        String name = "Duck of Doom";
        String description = "You and another player secretly compare hand. The player with the lower value "
                + "is out of the round.";
        return new DuckOfDoom(power, name, description);
    }
}

