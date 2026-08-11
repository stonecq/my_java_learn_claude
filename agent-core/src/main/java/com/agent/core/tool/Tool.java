package com.agent.core.tool;

import java.util.Map;

@FunctionalInterface
public interface Tool {
    String execute(Map<String, Object> params);

    default String getName(){
        return this.getClass().getSimpleName();
    };

    default ToolDescriptor getDescriptor(){
        return new ToolDescriptor(getName(), "No description provided.", Map.of());
    };
}
