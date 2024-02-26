package com.loot.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Boolean ready;

    @JsonProperty
    private String name;

    @JsonProperty
    private String image;

}
