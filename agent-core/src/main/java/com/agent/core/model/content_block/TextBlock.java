package com.agent.core.model.content_block;

public record TextBlock(String text) implements ContentBlock {
    public TextBlock{
        if (text == null || text.isBlank()){
            throw new IllegalArgumentException("文本内容不能为空");
        }
    }
}
