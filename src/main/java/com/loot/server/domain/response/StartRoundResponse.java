package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loot.server.domain.cards.Card;
import com.loot.server.domain.request.GamePlayer;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StartRoundResponse {

    private List<PlayerCardPair> playersAndCards;

    @JsonIgnore
    public StartRoundResponse(List<GamePlayer> players, List<Card> cards) {
        if(players.size() != cards.size()) {
            throw new RuntimeException("Invalid arguments passed to StartRoundResponse constructor!");
        }

        int length = players.size();
        final List<PlayerCardPair> playersAndCards = new ArrayList<>();
        for(int index = 0; index < length; index++) {
            playersAndCards.add(new PlayerCardPair(players.get(index), cards.get(index)));
        }
        this.playersAndCards = playersAndCards;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class PlayerCardPair {
        private GamePlayer player;
        private Card card;
    }
}
