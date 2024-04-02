package com.loot.server.domain.cards.cardresults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetTrollResult extends BaseCardResult {

    @JsonIgnore
    public NetTrollResult(GamePlayer playedOn, Card discardedCard, Card drawnCard) {
        super(playedOn);
        this.discardedCard = discardedCard;
        this.drawnCard = drawnCard;
    }

    @JsonProperty
    private Card discardedCard;

    @JsonProperty
    private Card drawnCard;
}
