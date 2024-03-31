package com.loot.server.domain.cards.cardresults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GazeboResult extends BaseCardResult {

    @JsonIgnore
    public GazeboResult(GamePlayer playedOn, Card opponentCard, Card playersCard) {
        super(playedOn);
        this.opponentCard = opponentCard;
        this.playersCard = playersCard;
    }

    private Card opponentCard;

    private Card playersCard;

}
