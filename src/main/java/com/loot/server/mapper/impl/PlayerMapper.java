package com.loot.server.mapper.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.dto.PlayerDto;
import com.loot.server.domain.entity.PlayerEntity;
import com.loot.server.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper implements Mapper<PlayerEntity, PlayerDto> {

    private final ModelMapper modelMapper;

    public PlayerMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    @Override
    public PlayerDto mapTo(PlayerEntity playerEntity) {
        return modelMapper.map(playerEntity, PlayerDto.class);
    }

    @Override
    public PlayerEntity mapFrom(PlayerDto playerDto) {
        return modelMapper.map(playerDto, PlayerEntity.class);
    }
}
