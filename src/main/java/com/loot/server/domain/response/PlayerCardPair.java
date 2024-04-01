package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerCardPair {

    @JsonInclude
    private GamePlayer player;

    @JsonInclude
    private Card card;

    @JsonIgnore
    @Override
    public String toString() {
        return "{ Player: " + player + ", Card: " +  card+ "}";
    }
}
