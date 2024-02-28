package com.loot.server.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.socket.logic.cards.BaseCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnResponse {

    @JsonProperty
    private BaseCard card;

    @JsonProperty
    private Boolean myTurn;
}
