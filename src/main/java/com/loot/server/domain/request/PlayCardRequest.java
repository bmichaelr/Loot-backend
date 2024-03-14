package com.loot.server.domain.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.domain.cards.PlayedCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayCardRequest {

    @JsonProperty
    private String roomKey;

    @JsonProperty
    private GamePlayer player;

    @JsonProperty
    private PlayedCard card;
}
