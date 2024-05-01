package com.loot.server.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamePlayer {

    @JsonProperty
    private UUID id;
    @JsonProperty
    private String name;
    @JsonProperty
    private Boolean ready;
    @JsonProperty
    private Boolean isSafe;
    @JsonProperty
    private Boolean isOut;
    @JsonProperty
    private Boolean isHost;
    @JsonProperty
    private String profileColor;
    @JsonProperty
    private Integer profilePicture;

    @JsonIgnore
    public GamePlayer(String name) {
        this.name = name;
        this.ready = false;
        this.isOut = false;
        this.isSafe = false;
        this.id = UUID.randomUUID();
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
                + ",\n\tsafe : " + this.isSafe + ",\n\tout: " + isOut + "\n}";
    }
    @JsonIgnore
    public GamePlayer copy() {
        return GamePlayer.builder()
                .name(this.name)
                .id(this.id)
                .build();
    }
}
