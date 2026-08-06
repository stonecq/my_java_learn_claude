package com.agent.model.content_block;

import java.util.Map;

public record ToolUseBlock(String id, String name, Map<String, Object> input) implements ContentBlock{
    public ToolUseBlock{
        if (id == null){
            throw new IllegalArgumentException("id不能为空");
        }
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("name 不能为空");
        }
    }
}
