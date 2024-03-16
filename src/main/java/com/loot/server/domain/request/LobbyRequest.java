package com.loot.server.domain.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LobbyRequest {

	@JsonProperty
	private GamePlayer player;

	@JsonProperty
	private String roomKey;

}
