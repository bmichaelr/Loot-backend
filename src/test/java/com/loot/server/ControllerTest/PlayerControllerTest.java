package com.loot.server.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loot.server.domain.entity.dto.PlayerDto;
import com.loot.server.service.PlayerControllerService;
import com.loot.server.ControllerTest.PlayerControllerTestUtil.PlayerCreationType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@AutoConfigureMockMvc
public class PlayerControllerTest {
    private final String CREATE_PLAYER_ENDPOINT = "/api/player/create";
    private final String GET_PLAYER_ENDPOINT = "/api/player/get";
    private final String UPDATE_PLAYER_ENDPOINT = "/api/player/update";
    private final String DELETE_PLAYER_ENDPOINT = "/api/player/delete";
    private final PlayerControllerService playerControllerService;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @Autowired
    public PlayerControllerTest(MockMvc mockMvc, PlayerControllerService playerControllerService) {
        this.playerControllerService = playerControllerService;
        this.mockMvc = mockMvc;
        objectMapper = new ObjectMapper();
    }
    @BeforeEach
    void makeMockPlayer() {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.MOCK_PLAYER);
        playerControllerService.createNewPlayer(playerDto);
    }
    @Test
    void testCreateReturnsHttp201Created() throws Exception {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.VALID);
        String json = objectMapper.writeValueAsString(playerDto);
        mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PLAYER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }
    @Test
    void testCreateReturnsHttp400WhenMissingParam() throws Exception {
        for(var creationType : PlayerCreationType.values()) {
            // Skip the types that would make valid DTO
            if(creationType == PlayerCreationType.VALID || creationType == PlayerCreationType.TAKEN_UNIQUE_NAME || creationType == PlayerCreationType.MOCK_PLAYER) {
                continue;
            }
            PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(creationType);
            String json = objectMapper.writeValueAsString(playerDto);
            mockMvc.perform(
                    MockMvcRequestBuilders.post(CREATE_PLAYER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
            ).andExpect(
                    MockMvcResultMatchers.status().isBadRequest()
            );
        }
    }
    @Test
    void testCreateReturnsHttp409WhenUniqueNameTaken() throws Exception {
        PlayerDto playerDto = PlayerControllerTestUtil.makePlayer(PlayerCreationType.TAKEN_UNIQUE_NAME);
        String json = objectMapper.writeValueAsString(playerDto);
        mockMvc.perform(
                MockMvcRequestBuilders.post(CREATE_PLAYER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(
                MockMvcResultMatchers.status().isConflict()
        );
    }
    @Test
    void testGetReturnsPlayerAndHttp200() throws Exception {
        final UUID uuid = PlayerControllerTestUtil.TEST_UUID;
        mockMvc.perform(
                MockMvcRequestBuilders.get(GET_PLAYER_ENDPOINT)
                        .param("id", uuid.toString())
        ).andExpectAll(
                MockMvcResultMatchers.status().isOk(),
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
        );
    }
    @Test
    void testGetReturnsHttp404WhenIdWrong() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get(GET_PLAYER_ENDPOINT)
                        .param("id", UUID.randomUUID().toString())
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    void testUpdateReturnsHttp200() throws Exception {
        final UUID uuid = PlayerControllerTestUtil.TEST_UUID;
        mockMvc.perform(
                MockMvcRequestBuilders.put(UPDATE_PLAYER_ENDPOINT)
                        .param("id", uuid.toString())
                        .param("name", "newName")
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }
    @Test
    void testUpdateReturnsHttp409WhenWrongId() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.put(UPDATE_PLAYER_ENDPOINT)
                        .param("id", UUID.randomUUID().toString())
                        .param("name", "newName")
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
    @Test
    void testDeleteReturnsHttp200() throws Exception {
        final UUID uuid = PlayerControllerTestUtil.TEST_UUID;
        mockMvc.perform(
                MockMvcRequestBuilders.delete(DELETE_PLAYER_ENDPOINT)
                        .param("id", uuid.toString())
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }
    @Test
    void testDeleteReturnsHttp409WhenWrongId() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete(DELETE_PLAYER_ENDPOINT)
                        .param("id", UUID.randomUUID().toString())
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }
}
