package com.loot.server.domain.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private UUID uuid;
    private String name;
    private String uniqueName;
    private Integer profilePicture;
    private String profileColor;
    @JsonIgnore
    public Boolean missingParameters() {
        return uuid == null || name == null || uniqueName == null || profilePicture == null || profileColor == null;
    }
}
