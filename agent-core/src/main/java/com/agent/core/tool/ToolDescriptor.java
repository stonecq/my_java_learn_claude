package com.agent.core.tool;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
public class ToolDescriptor {
    private String name;

    private String description;

    private Map<String, Object> inputSchema;
}
