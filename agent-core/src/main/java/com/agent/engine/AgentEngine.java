package com.agent.engine;

import com.agent.client.Client;
import com.agent.enums.StopReasonEnum;
import com.agent.model.AgentMessage;
import com.agent.model.AgentResponse;
import com.agent.model.AgentState;
import com.agent.model.content_block.ContentBlock;
import com.agent.model.content_block.ToolResultBlock;
import com.agent.model.content_block.ToolUseBlock;
import com.agent.permission.PermissionConfig;
import com.agent.permission.PermissionPipeline;
import com.agent.permission.PermissionResult;
import com.agent.permission.UserApprovalCallback;
import com.agent.tool.ToolRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AgentEngine {

    private final Client llmClient;

    private final ToolRegistry toolRegistry;

    private final AgentState agentState;

    private final PermissionPipeline permissionPipeline;

    private final int maxTurns;

    public AgentEngine(Path workdir, Client llmClient, ToolRegistry toolRegistry, UserApprovalCallback callback) {
        this(workdir, llmClient, toolRegistry, 50, callback);
    }

    public AgentEngine(Path workdir, Client llmClient, ToolRegistry toolRegistry, int maxTurns, UserApprovalCallback callback) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.agentState = new AgentState();
        this.maxTurns = maxTurns;
        this.permissionPipeline = PermissionConfig.withDefaults(workdir, callback);
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

            // Step 4: 逐个过权限检查后执行 tool_use 块，收集结果
            List<ToolResultBlock> toolResults = response.getContent().stream()
                    .filter(ToolUseBlock.class::isInstance)
                    .map(ToolUseBlock.class::cast)
                    .map(block -> {
                        PermissionResult result = permissionPipeline.check(block.name(), block.input());
                        if (result instanceof PermissionResult.Denied(String reason)) {
                            return new ToolResultBlock(block.id(), "权限拒绝: " + reason, true);
                        }
                        return toolRegistry.dispatch(block);
                    })
                    .toList();

            // Step 5: 以 user 角色把工具结果追加回消息，回到 Step 2
            List<ContentBlock> toolResultBlocks = new ArrayList<>(toolResults);
            messages.add(AgentMessage.forToolResults(toolResultBlocks));
        }
        return response;
    }
}
