package com.agent.subagent.tool;

import com.agent.core.client.Client;
import com.agent.core.hook.HookRegistry;
import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolComponent;
import com.agent.core.tool.ToolDescriptor;
import com.agent.core.tool.ToolRegistry;
import com.agent.subagent.SubagentEngine;
import com.agent.subagent.SubagentToolRegistry;

import java.util.List;
import java.util.Map;

@ToolComponent(subagentExcluded = true)
public class TaskTool implements Tool {
    private static final String PROMPT_PARAM = "prompt";

    private final Client llmClient;
    private final ToolRegistry parentToolRegistry;
    private final HookRegistry hookRegistry;
    private final int maxTokens;

    public TaskTool(Client llmClient, ToolRegistry parentToolRegistry, HookRegistry hookRegistry, int maxTokens) {
        this.llmClient = llmClient;
        this.parentToolRegistry = parentToolRegistry;
        this.hookRegistry = hookRegistry;
        this.maxTokens = maxTokens;
    }

    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get(PROMPT_PARAM) == null) {
            return "Error: 缺少 '" + PROMPT_PARAM + "' 参数";
        }
        String prompt = String.valueOf(params.get(PROMPT_PARAM));

        SubagentToolRegistry subTools = new SubagentToolRegistry();
        subTools.filterFromParent(parentToolRegistry);
        if (subTools.getAllDescriptors().isEmpty()) {
            return "Error: 子代理无可用工具";
        }

        SubagentEngine subagent = new SubagentEngine(llmClient, subTools, hookRegistry);
        return subagent.run(prompt, maxTokens);
    }

    @Override
    public String getName() {
        return "task";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "prompt", Map.of(
                        "type", "string",
                        "description", "交给子代理执行的任务描述"));
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of(PROMPT_PARAM));
        return new ToolDescriptor(getName(), "创建子代理执行委派任务并返回其总结", schema);
    }
}
