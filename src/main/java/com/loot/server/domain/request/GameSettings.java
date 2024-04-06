package com.loot.server.domain.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameSettings {

  @JsonProperty
  private String roomName;

  @JsonProperty
  private Integer numberOfPlayers;

  @JsonProperty
  private Integer numberOfWinsNeeded;

}
