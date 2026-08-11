package com.agent.core.enums;

import lombok.Getter;

@Getter
public enum AnthropicStopReasonEnum implements ValueNameEnum<String, String>{
    END_TURN("end_turn", "模型达到自然停止点"),
    MAX_TOKENS("max_tokens", "超过请求的 max_tokens 或模型的最大限制"),
    STOP_SEQUENCE("stop_sequence", "生成了自定义停止序列之一"),
    TOOL_USE("tool_use", "模型调用了一个或多个工具");

    private final String value;
    private final String name;

    AnthropicStopReasonEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
