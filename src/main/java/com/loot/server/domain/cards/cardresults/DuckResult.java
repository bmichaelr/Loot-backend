package com.loot.server.domain.cards.cardresults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuckResult extends BaseCardResult {

    @JsonIgnore
    public DuckResult(GamePlayer playedOn, Card opponentCard, Card playersCard, GamePlayer playerToDiscard) {
        super(playedOn);
        this.opponentCard = opponentCard;
        this.playersCard = playersCard;
        this.playerToDiscard = playerToDiscard;
    }

    private Card opponentCard;

    private Card playersCard;

    @JsonProperty
    private GamePlayer playerToDiscard;
}
