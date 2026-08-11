package com.agent.core.client;

import com.agent.core.exception.LLMClientException;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.AgentResponse;
import com.agent.core.tool.ToolDescriptor;

import java.util.List;

public class AnthropicLLMClient implements Client{
    String baseUrl = "https://api.deepseek.com/anthropic";
    String model = "deepseek-v4-flash";


    @Override
    public AgentResponse sendMessage(String system, List<AgentMessage> messages, List<ToolDescriptor> tools, int maxTokens) throws LLMClientException {
        return null;
    }
}
