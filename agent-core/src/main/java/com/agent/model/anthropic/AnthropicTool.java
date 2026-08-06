package com.agent.model.anthropic;

import com.agent.tool.ToolDescriptor;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AnthropicTool {
    private String name;

    private String description;

    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;

    public static AnthropicTool from(ToolDescriptor descriptor) {
        return new AnthropicTool(
                descriptor.getName(),
                descriptor.getDescription(),
                descriptor.getInputSchema()
        );
    }
}
