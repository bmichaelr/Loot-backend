package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TargetedEffectCard extends PlayedCard {

    @JsonProperty
    private Long playedOnId;

    @Override
    public String toString() {
        return "Targeted Effect Card:\n\tpower: " + this.getPower() + "\n\tplayedOnId: " + this.getPlayedOnId();
    }
}
