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
public class PottedResult extends BaseCardResult {

    @JsonIgnore
    public PottedResult(GamePlayer playedOn, Card guessedCard, Boolean correctGuess) {
        super(playedOn);
        this.guessedCard = guessedCard;
        this.correctGuess = correctGuess;
    }

    private Card guessedCard;

    private Boolean correctGuess;

}
