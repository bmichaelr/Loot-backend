package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.cards.Card;
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
    private Card card;

    @JsonProperty
    private Boolean myTurn;
}
