package com.loot.server.domain.response;

import com.loot.server.domain.request.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RoundStatusResponse {

    private GamePlayer winner;

    private Boolean gameOver;

    private Boolean roundOver;
}
