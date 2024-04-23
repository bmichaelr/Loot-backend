package com.loot.server.ControllerTest;

import com.loot.server.controllers.PlayerController;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.exceptions.PlayerControllerException;
import com.loot.server.service.PlayerControllerService;
import com.loot.server.ControllerTest.PlayerControllerTestUtil.PlayerCreationType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PlayerControllerTest {
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_RESET = "\u001B[0m";
    @Autowired
    private PlayerControllerService playerControllerService;
    @InjectMocks
    private PlayerController playerController;
    @BeforeEach
    void makeMockPlayer() {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.MOCK_PLAYER);
        try {
            playerControllerService.createNewPlayer(playerDto);
            System.out.println(ANSI_GREEN + "[SUCCESS] " + ANSI_RESET + " :: Player created successfully in makeMockPlayer() (@beforeEach)");
        } catch (PlayerControllerException exception) {
            System.out.println(ANSI_RED + "[FAILURE] " + ANSI_RESET + ":: Unable to create player! Exception(" + exception.getMessage() + ")");
        }
    }
    @Test
    void testCreateDoesNotThrowException() throws PlayerControllerException {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.VALID);
        assertDoesNotThrow(() -> playerControllerService.createNewPlayer(playerDto));
    }
    @Test
    void testCreateThrowsWhenMissingParam() throws PlayerControllerException {
        for (var creationType : PlayerCreationType.values()) {
            if (creationType == PlayerCreationType.VALID || creationType == PlayerCreationType.TAKEN_UNIQUE_NAME || creationType == PlayerCreationType.MOCK_PLAYER) {
                continue;
            }
            PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(creationType);
            assertThrows(PlayerControllerException.class, () -> playerControllerService.createNewPlayer(playerDto));
        }
    }
    @Test
    void testCreateThrowsWhenUniqueUserNameTaken() throws Exception {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.TAKEN_UNIQUE_NAME);
        assertThrows(PlayerControllerException.class, () -> playerControllerService.createNewPlayer(playerDto));
    }
    @Test
    void testGetReturnsPlayerDtoAndDoesNotThrow() {
        final UUID uuid = PlayerControllerTestUtil.TEST_UUID;
        assertDoesNotThrow(() -> {
            PlayerDto playerDto = playerControllerService.getExistingPlayer(uuid);
            assertNotNull(playerDto);
        });

    }
    @Test
    void testGetThrowsWhenWrongId() {
        final UUID uuid = UUID.randomUUID();
        assertThrows(PlayerControllerException.class, () -> playerControllerService.getExistingPlayer(uuid));
    }
    @Test
    void testUpdateDoesNotThrowWhenCorrect() {
        final PlayerDto update = PlayerControllerTestUtil.makePlayer(PlayerCreationType.MOCK_PLAYER);
        update.setName("New Name");
        assertDoesNotThrow(() -> {
            PlayerDto playerDto = playerControllerService.updatePlayerName(update);
            assertNotNull(playerDto);
            assertEquals(playerDto.getName(), "New Name");
        });
    }
    @Test
    void testUpdateThrowsWhenWrongId() {
        final PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.MOCK_PLAYER);
        playerDto.setUuid(UUID.randomUUID());
        assertThrows(PlayerControllerException.class, () -> playerControllerService.updatePlayerName(playerDto));
    }
    @Test
    void testDeleteDoesNotThrow() throws Exception {
        final UUID uuid = PlayerControllerTestUtil.TEST_UUID;
        assertDoesNotThrow(() -> playerControllerService.deletePlayerAccount(uuid));
    }
    @Test
    void testDeleteThrowsWhenWrongId() throws Exception {
        assertThrows(PlayerControllerException.class, () -> playerControllerService.deletePlayerAccount(UUID.randomUUID()));
    }
}
