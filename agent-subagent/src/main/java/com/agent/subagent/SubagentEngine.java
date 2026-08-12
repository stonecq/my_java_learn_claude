package com.agent.subagent;

import com.agent.core.client.Client;
import com.agent.core.enums.StopReasonEnum;
import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookRegistry;
import com.agent.core.hook.HookResult;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.AgentResponse;
import com.agent.core.model.content_block.ContentBlock;
import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.agent.core.utils.StringUtils.truncate;

public class SubagentEngine {
    private static final Logger log = LoggerFactory.getLogger(SubagentEngine.class);

    private static final int MAX_TURNS = 30;

    private static final String SUB_SYSTEM_PROMPT =
            "You are a coding agent. Complete the task you were given, " +
                    "then return a concise summary. Do not delegate further.";

    private final Client llmClient;
    private final SubagentToolRegistry toolRegistry;
    private final HookRegistry hookRegistry;
    public SubagentEngine(Client llmClient, SubagentToolRegistry toolRegistry, HookRegistry hookRegistry) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.hookRegistry = hookRegistry;
    }

    public String run(String description, int maxTokens){
        log.info("[Subagent] spawned for task: {}", truncate(description, 80));
        List<AgentMessage> messages = new ArrayList<>();
        messages.add(AgentMessage.forUser(description));

        AgentResponse response = null;
        for (int i = 0; i < MAX_TURNS; i++){
            response = llmClient.sendMessage(SUB_SYSTEM_PROMPT, messages, toolRegistry.getAllDescriptors(), maxTokens);

            messages.add(AgentMessage.forAssistant(response.getContent()));
            if (!StopReasonEnum.TOOL_USE.getValue().equals(response.getStopReason())){
                break;
            }
            List<ToolResultBlock> toolResults = new ArrayList<>();
            for (ContentBlock block : response.getContent()){
                if (!(block instanceof ToolUseBlock toolUseBlock)){
                    continue;
                }
                HookResult preHook = hookRegistry.trigger(HookEvent.PRE_TOOL_USE, toolUseBlock);
                if (preHook instanceof HookResult.Blocked(String reason)){
                    toolResults.add(new ToolResultBlock(toolUseBlock.id(), "拦截: " + reason, true));
                    continue;
                }
                ToolResultBlock toolResultBlock = toolRegistry.dispatch(toolUseBlock);
                hookRegistry.trigger(HookEvent.POST_TOOL_USE, block, toolResultBlock);
                toolResults.add(toolResultBlock);
            }
            List<ContentBlock> resultBlocks = new ArrayList<>(toolResults);
            messages.add(AgentMessage.forToolResults(resultBlocks));
        }
        String summary = SubagentTextExtractor.extract(messages);
        log.info("[Subagent] done, summary length: {}", summary.length());
        return summary;
    }
}
