package com.loot.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DialectOverride;

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
    private Boolean loadedIn;

    @JsonProperty
    private Boolean isSafe;

    @JsonProperty
    private String name;

    @JsonIgnore
    public GamePlayer(PlayerDto playerDto) {
        this.id = playerDto.getId();
        this.name = playerDto.getName();
        this.ready = false;
        this.isSafe = false;
    }

    @JsonIgnore
    public GamePlayer(PlayerDto playerDto, boolean ready) {
        this.id = playerDto.getId();
        this.name = playerDto.getName();
        this.ready = ready;
        this.isSafe = false;
    }

    @JsonIgnore
    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }

        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        GamePlayer player = (GamePlayer) obj;
        return (player.getId().equals(this.getId()) && player.getName().equals(this.getName()));
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        return id.hashCode() + name.hashCode();
    }

    @JsonIgnore
    @Override
    public String toString(){
        return "Game Player : {\n\tname : " + this.name + ",\n\tid : " + this.id + ",\n\tready : " + this.ready
                + ",\n\tloadedIn: " + this.loadedIn + ",\n\tsafe : " + this.isSafe + "\n}";
    }

    @JsonIgnore
    public GamePlayer copy() {
        return GamePlayer.builder()
                .name(this.name)
                .id(this.id)
                .build();
    }
}
