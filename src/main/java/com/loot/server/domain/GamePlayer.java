package com.loot.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamePlayer {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Boolean ready;

    @JsonProperty
    private String name;

    @JsonIgnore
    public GamePlayer(PlayerDto playerDto) {
        this.id = playerDto.getId();
        this.name = playerDto.getName();
        this.ready = false;
    }

    @JsonIgnore
    public GamePlayer(PlayerDto playerDto, boolean ready) {
        this.id = playerDto.getId();
        this.name = playerDto.getName();
        this.ready = ready;
    }
}
