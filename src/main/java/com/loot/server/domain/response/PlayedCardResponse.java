package com.loot.server.domain.response;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.cards.cardresults.BaseCardResult;
import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayedCardResponse {
    private String type;
    private GamePlayer playerWhoPlayed;
    private Card cardPlayed;
    private Boolean waitFlag;
    private BaseCardResult outcome;
    public static PlayedCardResponseBuilder builder() {
        return new CustomBuilder();
    }
    public static class CustomBuilder extends PlayedCardResponseBuilder {
        @Override
        public PlayedCardResponse build() {
            PlayedCardResponse response = super.build();
            response.setType(response.getOutcome().getType());
            return response;
        }
    }
}
