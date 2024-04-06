package com.loot.server.domain.cards.cardresults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class MaulRatResult extends BaseCardResult {

    @JsonIgnore
    public MaulRatResult(GamePlayer playedOn, Card opponentsCard) {
        super(playedOn, "maulRat");
        this.opponentsCard = opponentsCard;
    }

    private Card opponentsCard;
}
