package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoundStatusResponse {
    @JsonProperty
    private GamePlayer winner;
    @JsonProperty
    private Boolean gameOver;
    @JsonProperty
    private Boolean roundOver;
    @JsonProperty
    private Card winningCard;
}
