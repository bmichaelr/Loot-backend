package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameStatus {

    @JsonProperty
    private String roomKey;

    @JsonProperty
    private List<PlayerDto> players;

    @JsonIgnore
    public void addPlayer(PlayerDto playerDto) {
        if(players == null) {
            players = new ArrayList<>();
        }

        players.add(playerDto);
    }
}