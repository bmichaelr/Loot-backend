package com.loot.server.socket.logic.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Card {

    private int power;

    private String name;

    private String description;

    public static Card cardFromPower(int power) {
        return switch (power) {
            case 1 -> pottedPlant();
            case 2 -> maulRat();
            case 3 -> duckOfDoom();
            case 4 -> wishingRing();
            case 5 -> netTroll();
            case 6 -> dreadGazebo();
            case 7 -> turboniumDragon();
            case 8 -> loot();
            default -> throw new IllegalStateException("Unexpected card value: " + power);
        };
    }

    @JsonIgnore
    public static Card pottedPlant(){
        int power = 1;
        String name = "Potted Plant";
        String description = "Name a non-Potted Plant card and choose another player. If that player has that card, "
                + "he or she is out of the round.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card maulRat(){
        int power = 2;
        String name = "Maul Rat";
        String description = "Look at another player's hand.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card duckOfDoom(){
        int power = 3;
        String name = "Duck of Doom";
        String description = "You and another player secretly compare hand. The player with the lower value "
                + "is out of the round.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card wishingRing(){
        int power = 4;
        String name = "Wishing Ring";
        String description = "Until your next turn, ignore all effects from other players' cards.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card netTroll(){
        int power = 5;
        String name = "Net Troll";
        String description = "Choose any player (including yourself) to discard his or her hand and draw a new card";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card dreadGazebo(){
        int power = 6;
        String name = "Dread Gazebo";
        String description = "Trade hands with a player of your choice.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card turboniumDragon(){
        int power = 7;
        String name = "Turbonium Dragon";
        String description = "If you have this card and the Dread Gazebo or Net Troll in your hand, you must "
                + "discard this card.";
        return new Card(power, name, description);
    }

    @JsonIgnore
    public static Card loot(){
        int power = 8;
        String name = "Loot!";
        String description = "If you discard this card, you are out of the round.";
        return new Card(power, name, description);
    }
}
