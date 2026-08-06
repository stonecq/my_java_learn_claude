package com.agent.model.content_block;

public record ToolResultBlock(String id, String content, Boolean isError) implements ContentBlock{
    public ToolResultBlock{
        if (id == null){
            throw new IllegalArgumentException("id不能为空");
        }
        if (content == null || content.isBlank()){
            throw new IllegalArgumentException("content 不能为空");
        }
    }
}
