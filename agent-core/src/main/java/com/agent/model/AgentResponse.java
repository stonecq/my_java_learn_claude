package com.agent.model;

import com.agent.model.content_block.ContentBlock;
import com.agent.model.content_block.ToolUseBlock;
import lombok.Data;

import java.util.List;
/*
    封装调用llm返回的结果，类似与执行结果报告
 */
@Data
public class AgentResponse {
    List<ContentBlock> content;
    Integer stopReason;

    /**
     * token 使用量
     */
    Usage usage;

    /**
     * 模型使用名称
     */
    String model;

    @Data
    public static class Usage{
        public int inputTokens;
        public int outputTokens;
    }
}
