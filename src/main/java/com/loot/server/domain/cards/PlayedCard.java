package com.loot.server.domain.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayedCard.class, name = "personal"),
        @JsonSubTypes.Type(value = TargetedEffectCard.class, name = "targeted"),
        @JsonSubTypes.Type(value = GuessingCard.class, name = "guess")
})
public class PlayedCard {

    @JsonProperty
    private int power;

    @Override
    public String toString() {
        return "Played Card:\n\tpower: " + this.getPower();
    }
}