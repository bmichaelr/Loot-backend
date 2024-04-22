package com.loot.server.repositories;

import com.loot.server.domain.entity.LeaderboardEntryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardRepository extends CrudRepository<LeaderboardEntryEntity, Long> {
    @Query(
            value = "SELECT * FROM leaderboard where player = :id LIMIT 1",
            nativeQuery = true
    )
    Optional<LeaderboardEntryEntity> findEntryByPlayerId(@Param("id") UUID playerId);
}
