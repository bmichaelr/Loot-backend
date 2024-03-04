package com.loot.server.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.socket.logic.cards.Card;
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
    private PlayerDto player;

    @JsonProperty
    private Card card;
}
