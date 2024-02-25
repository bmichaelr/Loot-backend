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
public class NetTroll extends BaseCard {

    @JsonProperty
    private String playedOn;

    public NetTroll(int power, String name, String description) {
        super(power, name, description);
    }

    public NetTroll(int power, String name, String description, String playedOn) {
        super(power, name, description);
        this.playedOn = playedOn;
    }

    @JsonIgnore
    public static NetTroll createCard(){
        int power = 5;
        String name = "Net Troll";
        String description = "Choose any player (including yourself) to discard his or her hand and draw a new card";
        return new NetTroll(power, name, description);
    }
}

