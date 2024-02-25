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
public class Loot extends BaseCard {

    public Loot(int power, String name, String description) {
        super(power, name, description);
    }

    @JsonIgnore
    public static Loot createCard(){
        int power = 8;
        String name = "Loot!";
        String description = "If you discard this card, you are out of the round.";
        return new Loot(power, name, description);
    }
}

