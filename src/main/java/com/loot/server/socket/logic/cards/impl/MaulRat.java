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
public class MaulRat extends BaseCard {

    @JsonProperty
    private String playedOn;

    public MaulRat(int power, String name, String description) {
        super(power, name, description);
    }

    public MaulRat(int power, String name, String description, String playedOn) {
        super(power, name, description);
        this.playedOn = playedOn;
    }

    @JsonIgnore
    public static MaulRat createCard(){
        int power = 2;
        String name = "Maul Rat";
        String description = "Look at another player's hand.";
        return new MaulRat(power, name, description);
    }
}
