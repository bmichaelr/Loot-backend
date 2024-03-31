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

    private GamePlayer playerWhoPlayed;

    private Card cardPlayed;

    private Boolean waitFlag;

    private BaseCardResult outcome;
}
