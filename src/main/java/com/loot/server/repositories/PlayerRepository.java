package com.loot.server.repositories;

import com.loot.server.domain.entity.PlayerEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends CrudRepository<PlayerEntity, Long> {
    @Query (
            value = "SELECT * FROM users WHERE uniqueName = :uniqueName LIMIT 1",
            nativeQuery = true
    )
    Optional<PlayerEntity> findPlayerByUniqueName(@Param("uniqueName") String uniqueName);
    @Query (
            value = "SELECT * FROM users WHERE clientId = :id LIMIT 1",
            nativeQuery = true
    )
    Optional<PlayerEntity> findPlayerEntityByClientId(@Param("id") UUID id);
}
