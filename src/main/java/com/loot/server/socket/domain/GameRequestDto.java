package com.loot.server.socket.domain;

import com.loot.server.domain.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRequestDto {

	private PlayerDto playerDto;

	private String roomKey;

}
