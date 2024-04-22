package com.loot.server.ServiceTests;

import com.loot.server.domain.entity.dto.LeaderboardEntryDto;
import com.loot.server.service.LeaderboardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class LeaderboardServiceTest {
    @Autowired
    private LeaderboardService leaderboardService;

    @Test
    void testSortingMethodWorks() {
        List<LeaderboardEntryDto> entriesToSort = new java.util.ArrayList<>(List.of(
                new LeaderboardEntryDto("Ben", 5L),
                new LeaderboardEntryDto("Josh", 1L),
                new LeaderboardEntryDto("Kenna", 1L),
                new LeaderboardEntryDto("Ian", 3L),
                new LeaderboardEntryDto("Caleb", 4L),
                new LeaderboardEntryDto("Bernie", 10L),
                new LeaderboardEntryDto("John", 6L),
                new LeaderboardEntryDto("Clark", 15L),
                new LeaderboardEntryDto("Scooby Doo", 2L)
        ));
        entriesToSort.sort((a, b) -> Long.compare(b.getNumberOfWins(), a.getNumberOfWins()));
        List<LeaderboardEntryDto> expectedSortedEntries = List.of(
                new LeaderboardEntryDto("Clark", 15L),
                new LeaderboardEntryDto("Bernie", 10L),
                new LeaderboardEntryDto("John", 6L),
                new LeaderboardEntryDto("Ben", 5L),
                new LeaderboardEntryDto("Caleb", 4L),
                new LeaderboardEntryDto("Ian", 3L),
                new LeaderboardEntryDto("Scooby Doo", 2L),
                new LeaderboardEntryDto("Josh", 1L),
                new LeaderboardEntryDto("Kenna", 1L)
        );
        Assertions.assertEquals(expectedSortedEntries, entriesToSort);
    }

}
