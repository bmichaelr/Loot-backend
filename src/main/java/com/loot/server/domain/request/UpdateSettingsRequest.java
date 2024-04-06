package com.loot.server.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateSettingsRequest {

    private GamePlayer player;

    private GameSettings settings;

    private String roomKey;
}
