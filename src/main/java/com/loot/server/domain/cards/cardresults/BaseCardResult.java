package com.loot.server.domain.cards.cardresults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCardResult {

    private GamePlayer playedOn;

    @JsonIgnore
    private String type;

    public BaseCardResult(GamePlayer playedOn) {
        this.playedOn = playedOn;
        this.type = "base";
    }
}
