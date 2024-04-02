package com.loot.server.domain.cards.cardresults;

import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCardResult {

    private GamePlayer playedOn;
}
