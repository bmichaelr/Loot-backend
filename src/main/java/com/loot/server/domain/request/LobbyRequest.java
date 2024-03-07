package com.loot.server.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.loot.server.domain.dto.PlayerDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LobbyRequest {

	@JsonProperty
	private PlayerDto playerDto;

	@JsonProperty
	private String roomKey;

}
