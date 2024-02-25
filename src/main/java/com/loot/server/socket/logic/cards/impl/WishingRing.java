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
public class WishingRing extends BaseCard {

    public WishingRing(int power, String name, String description) {
        super(power, name, description);
    }

    @JsonIgnore
    public static WishingRing createCard(){
        int power = 4;
        String name = "Wishing Ring";
        String description = "Until your next turn, ignore all effects from other players' cards.";
        return new WishingRing(power, name, description);
    }
}

