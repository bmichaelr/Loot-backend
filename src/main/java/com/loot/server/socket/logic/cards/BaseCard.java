package com.loot.server.socket.logic.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.loot.server.socket.logic.cards.impl.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PottedPlant.class, name = "pottedPlant"),
        @JsonSubTypes.Type(value = MaulRat.class, name = "maulRat"),
        @JsonSubTypes.Type(value = DuckOfDoom.class, name = "duckOfDoom"),
        @JsonSubTypes.Type(value = WishingRing.class, name = "wishingRing"),
        @JsonSubTypes.Type(value = NetTroll.class, name = "netTroll"),
        @JsonSubTypes.Type(value = DreadGazebo.class, name = "dreadGazebo"),
        @JsonSubTypes.Type(value = TurboniumDragon.class, name = "turboniumDragon"),
        @JsonSubTypes.Type(value = Loot.class, name = "loot")
})
public class BaseCard {

    private int power;

    private String name;

    private String description;
}
