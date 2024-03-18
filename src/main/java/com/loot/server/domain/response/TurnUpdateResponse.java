package com.loot.server.domain.response;

import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TurnUpdateResponse {

    private String message;

    private Map<GamePlayer, List<Card>> cards;

    // ? private Map<GamePlayer, Integer> wins;

    private Boolean roundOver;

    private Boolean gameOver;
}
