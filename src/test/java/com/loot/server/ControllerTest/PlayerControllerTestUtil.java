package com.loot.server.ControllerTest;

import com.loot.server.domain.entity.dto.PlayerDto;

import java.util.UUID;

public class PlayerControllerTestUtil {
    final static UUID TEST_UUID = UUID.randomUUID();
    final static String TEST_UNIQUE_NAME = "test#1234";
    final static String TEST_NAME = "test";
    public enum PlayerCreationType {
        MOCK_PLAYER,
        VALID,
        TAKEN_UNIQUE_NAME,
        MISSING_NAME,
        MISSING_UUID,
        MISSING_UNIQUE_NAME,
        MISSING_PROFILE_COLOR,
        MISSING_PROFILE_PICTURE
    }
    public static PlayerDto makePlayer(PlayerCreationType creationType) {
        return PlayerDto.builder()
                .uuid(creationType == PlayerCreationType.MISSING_UUID ? null : TEST_UUID)
                .name(creationType == PlayerCreationType.MISSING_NAME ? null : TEST_NAME)
                .uniqueName(
                        switch(creationType) {
                            case TAKEN_UNIQUE_NAME, MOCK_PLAYER -> TEST_UNIQUE_NAME;
                            case MISSING_UNIQUE_NAME -> null;
                            default -> "test#09KL";
                        }
                )
                .profileColor(creationType == PlayerCreationType.MISSING_PROFILE_COLOR ? null : "#FFFFFF")
                .profilePicture(creationType == PlayerCreationType.MISSING_PROFILE_PICTURE? null : 8)
                .build();
    }
}
