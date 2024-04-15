package com.loot.server.mappers.impl;

import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapperImpl implements Mapper<PlayerEntity, PlayerDto> {

    private final ModelMapper mapper;

    public PlayerMapperImpl(ModelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PlayerDto mapTo(PlayerEntity playerEntity) {
        return mapper.map(playerEntity, PlayerDto.class);
    }

    @Override
    public PlayerEntity mapFrom(PlayerDto playerDto) {
        return mapper.map(playerDto, PlayerEntity.class);
    }
}
