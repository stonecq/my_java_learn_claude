package com.agent.engine;

import com.agent.client.Client;
import com.agent.enums.StopReasonEnum;
import com.agent.hook.HookEvent;
import com.agent.hook.HookRegistry;
import com.agent.hook.HookResult;
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

    private final HookRegistry hookRegister;

    private final AgentState agentState;

    private final int maxTurns;

    public AgentEngine(Client llmClient, ToolRegistry toolRegistry, HookRegistry hookRegister) {
        this(llmClient, toolRegistry, hookRegister, 50);
    }

    public AgentEngine(Client llmClient, ToolRegistry toolRegistry, HookRegistry hookRegister, int maxTurns) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentState = new AgentState();
        this.maxTurns = maxTurns;
        this.hookRegister = hookRegister;
    }

    public AgentResponse run(String systemPrompt, String userMessage, int maxTokens) {
        // Step 1: 初始化状态，把用户提问作为第一条消息
        List<AgentMessage> messages = new ArrayList<>();
        messages.add(AgentMessage.forUser(userMessage));
        agentState.setMessages(messages);
        agentState.setTurnCount(0);
        hookRegister.trigger(HookEvent.USER_PROMPT_SUBMIT, userMessage);

        AgentResponse response = null;
        while (agentState.getTurnCount() < maxTurns) {
            // Step 2: 调用 LLM
            response = llmClient.sendMessage(systemPrompt, messages, toolRegistry.getAllDescriptors(), maxTokens);
            agentState.setTurnCount(agentState.getTurnCount() + 1);

            // Step 3: 追加 assistant 响应；没有 tool_use 块说明模型正常回答完毕，结束循环
            messages.add(AgentMessage.forAssistant(response.getContent()));
            if (!StopReasonEnum.TOOL_USE.getValue().equals(response.getStopReason())) {
                hookRegister.trigger(HookEvent.STOP, messages);
                return response;
            }

            // Step 4: PRE_TOOL_USE 拦截 -> 执行工具 -> POST_TOOL_USE 收尾，收集结果
            List<ToolResultBlock> toolResults = response.getContent().stream()
                    .filter(ToolUseBlock.class::isInstance)
                    .map(ToolUseBlock.class::cast)
                    .map(block -> {
                        HookResult pre = hookRegister.trigger(HookEvent.PRE_TOOL_USE, block);
                        if (pre instanceof HookResult.Blocked(String reason)) {
                            return new ToolResultBlock(block.id(), "拦截: " + reason, true);
                        }
                        ToolResultBlock result = toolRegistry.dispatch(block);
                        hookRegister.trigger(HookEvent.POST_TOOL_USE, block, result);
                        return result;
                    })
                    .toList();

            // Step 5: 以 user 角色把工具结果追加回消息，回到 Step 2
            List<ContentBlock> toolResultBlocks = new ArrayList<>(toolResults);
            messages.add(AgentMessage.forToolResults(toolResultBlocks));
        }
        hookRegister.trigger(HookEvent.STOP, messages);
        return response;
    }
}
