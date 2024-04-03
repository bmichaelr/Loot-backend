package com.loot.server.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ObjectDecodeTestRequest {

    private String type;

    private String id;
}
