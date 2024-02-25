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
public class DreadGazebo extends BaseCard {

    @JsonProperty
    private String playedOn;

    public DreadGazebo(int power, String name, String description) {
        super(power, name, description);
    }

    public DreadGazebo(int power, String name, String description, String playedOn) {
        super(power, name, description);
        this.playedOn = playedOn;
    }

    @JsonIgnore
    public static DreadGazebo createCard(){
        int power = 6;
        String name = "Dread Gazebo";
        String description = "Trade hands with a player of your choice.";
        return new DreadGazebo(power, name, description);
    }
}

