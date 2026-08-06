package com.agent.client;

import com.agent.exception.LLMClientException;
import com.agent.model.AgentMessage;
import com.agent.model.AgentResponse;
import com.agent.tool.ToolDescriptor;

import java.util.List;

public interface Client {
    AgentResponse sendMessage(String system, // 系统提示词
                              List<AgentMessage> messages, // 累积消息历史
                              List<ToolDescriptor> tools, // 可用工具描述列表
                              int maxTokens // 最大输出 token 数
    ) throws LLMClientException;

}
