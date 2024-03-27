package com.loot.server.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerData {

    @JsonProperty
    private String name;

    @JsonProperty
    private String key;

    @JsonProperty
    private Integer maximumPlayers;

    @JsonProperty
    private Integer numberOfPlayers;

    @JsonProperty
    private String status;

}
