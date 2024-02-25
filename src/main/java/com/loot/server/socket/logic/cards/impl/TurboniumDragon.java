package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.loot.server.socket.logic.cards.BaseCard;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TurboniumDragon extends BaseCard {

    public TurboniumDragon(int power, String name, String description) {
        super(power, name, description);
    }

    @JsonIgnore
    public static TurboniumDragon createCard(){
        int power = 7;
        String name = "Turbonium Dragon";
        String description = "If you have this card and the Dread Gazebo or Net Troll in your hand, you must "
                + "discard this card.";
        return new TurboniumDragon(power, name, description);
    }
}

