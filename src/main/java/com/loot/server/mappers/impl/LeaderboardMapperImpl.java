package com.loot.server.mappers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.entity.LeaderboardEntryEntity;
import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardMapperImpl implements Mapper<LeaderboardEntryEntity, LeaderboardEntryDto> {
    private final ModelMapper mapper;
    public LeaderboardMapperImpl() {
        this.mapper = new ModelMapper();
    }
    @Override
    public LeaderboardEntryDto mapTo(LeaderboardEntryEntity leaderboardEntryEntity) {
        return mapper.map(leaderboardEntryEntity, LeaderboardEntryDto.class);
    }
    @Override
    public LeaderboardEntryEntity mapFrom(LeaderboardEntryDto leaderboardEntryDto) {
        return mapper.map(leaderboardEntryDto, LeaderboardEntryEntity.class);
    }
}
