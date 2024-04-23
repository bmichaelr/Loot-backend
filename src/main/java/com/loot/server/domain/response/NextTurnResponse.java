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
public class NextTurnResponse {
    private GamePlayer player;
    private Card card;
}
