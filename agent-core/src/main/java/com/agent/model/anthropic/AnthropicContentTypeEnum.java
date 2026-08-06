package com.agent.model.anthropic;

import com.agent.enums.ValueNameEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnthropicContentTypeEnum implements ValueNameEnum<String, String> {
    TEXT("text", "文本"),
    TOOL_USE("tool_use", "工具使用"),
    TOOL_RESULT("tool_result", "工具结果"),
    ;
    final String value;

    final String name;
}
