package com.agent.subagent;

import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolComponent;
import com.agent.core.tool.ToolDescriptor;
import com.agent.core.tool.ToolRegistry;

import java.util.*;

public class SubagentToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();
    private final Map<String, ToolDescriptor> descriptors = new HashMap<>();

    public void filterFromParent(ToolRegistry parentRegistry) {
        for (ToolDescriptor descriptor : parentRegistry.getAllDescriptors()) {
            Tool tool = parentRegistry.getTool(descriptor.getName());
            if (tool == null) {
                continue;
            }
            ToolComponent toolComponent = tool.getClass().getAnnotation(ToolComponent.class);
            if (toolComponent != null && toolComponent.subagentExcluded()) {
                continue;
            }
            tools.put(tool.getName(), tool);
            descriptors.put(tool.getName(), tool.getDescriptor());
        }
    }

    public ToolResultBlock dispatch(ToolUseBlock block) {
        Tool tool = tools.get(block.name());
        if (tool == null) {
            return new ToolResultBlock(block.id(), "Error: 未注册的工具: " + block.name(), true);
        }
        String result = tool.execute(block.input());
        return new ToolResultBlock(block.id(), result, null);
    }

    public List<ToolDescriptor> getAllDescriptors() {
        return new ArrayList<>(descriptors.values());
    }
}
