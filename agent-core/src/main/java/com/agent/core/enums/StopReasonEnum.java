package com.agent.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StopReasonEnum implements ValueNameEnum<Integer, String>{
    TOOL_USE(1,"模型请求工具"),       // 模型请求工具 → 循环继续
    END_TURN(2,"模型完成回答"),       // 模型完成回答 → 循环结束
    MAX_TOKENS(3,"达到 token 上限"),   // 达到 token 上限 → 循环结束
    STOP_SEQUENCE(4,"命中停止序列"); // 命中停止序列 → 循环结束
    ;

    final Integer value;
    final String name;
}
