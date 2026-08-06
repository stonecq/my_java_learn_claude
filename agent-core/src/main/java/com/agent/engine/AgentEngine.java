package com.agent.engine;

import com.agent.client.Client;
import com.agent.enums.StopReasonEnum;
import com.agent.model.AgentMessage;
import com.agent.model.AgentResponse;
import com.agent.model.AgentState;
import com.agent.model.content_block.ContentBlock;
import com.agent.model.content_block.ToolResultBlock;
import com.agent.model.content_block.ToolUseBlock;
import com.agent.tool.ToolRegistry;

import java.util.ArrayList;
import java.util.List;

public class AgentEngine {

    private final Client llmClient;

    private final ToolRegistry toolRegistry;

    private final AgentState agentState;

    private final int maxTurns;

    public AgentEngine(Client llmClient, ToolRegistry toolRegistry) {
        this(llmClient, toolRegistry, 50);
    }

    public AgentEngine(Client llmClient, ToolRegistry toolRegistry, int maxTurns) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentState = new AgentState();
        this.maxTurns = maxTurns;
    }

    public AgentResponse run(String systemPrompt, String userMessage, int maxTokens) {
        // Step 1: 初始化状态，把用户提问作为第一条消息
        List<AgentMessage> messages = new ArrayList<>();
        messages.add(AgentMessage.forUser(userMessage));
        agentState.setMessages(messages);
        agentState.setTurnCount(0);

        AgentResponse response = null;
        while (agentState.getTurnCount() < maxTurns) {
            // Step 2: 调用 LLM
            response = llmClient.sendMessage(systemPrompt, messages, toolRegistry.getAllDescriptors(), maxTokens);
            agentState.setTurnCount(agentState.getTurnCount() + 1);

            // Step 3: 追加 assistant 响应；没有 tool_use 块说明模型正常回答完毕，结束循环
            messages.add(AgentMessage.forAssistant(response.getContent()));
            if (!StopReasonEnum.TOOL_USE.getValue().equals(response.getStopReason())) {
                return response;
            }

            // Step 4: 筛选出 tool_use 块并逐个执行，收集结果
            List<ToolResultBlock> toolResults = response.getContent().stream()
                    .filter(ToolUseBlock.class::isInstance)
                    .map(ToolUseBlock.class::cast)
                    .map(toolRegistry::dispatch)
                    .toList();

            // Step 5: 以 user 角色把工具结果追加回消息，回到 Step 2
            List<ContentBlock> toolResultBlocks = new ArrayList<>(toolResults);
            messages.add(AgentMessage.forToolResults(toolResultBlocks));
        }
        return response;
    }
}
