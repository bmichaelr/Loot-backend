package com.loot.server.socket.logic.cards.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.GamePlayer;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TargetedEffectCard extends PlayedCard {

    @JsonIgnore
    public TargetedEffectCard(int playedCard, GamePlayer playedOn) {
        super(playedCard);
        this.playedOn = playedOn;
    }

    @JsonProperty
    private GamePlayer playedOn;

    @Override
    public String toString() {
        return "Targeted Effect Card:\n\tpower: " + this.getPower() + "\n\tplayedOn: " + this.getPlayedOn();
    }
}
