package com.loot.server.domain.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {

    private UUID uuid;
    private String name;
    private Integer profilePicture;
    private String profileColor;
}
