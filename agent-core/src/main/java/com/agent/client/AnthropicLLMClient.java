package com.agent.client;

import com.agent.exception.LLMClientException;
import com.agent.model.AgentMessage;
import com.agent.model.AgentResponse;
import com.agent.tool.ToolDescriptor;

import java.util.List;

public class AnthropicLLMClient implements Client{
    String baseUrl = "https://api.deepseek.com/anthropic";
    String model = "deepseek-v4-flash";


    @Override
    public AgentResponse sendMessage(String system, List<AgentMessage> messages, List<ToolDescriptor> tools, int maxTokens) throws LLMClientException {
        return null;
    }
}
