package com.agent.model.anthropic;

import lombok.Data;

import java.util.List;

@Data
public class AnthropicRequest {
    String model;

    String system;

    List<AnthropicMessage> messages;

    Integer maxTokens;

    List<AnthropicTool> tools;

}
