package com.agent.hook.hook;

import com.agent.hook.Hook;
import com.agent.hook.HookResult;
import com.agent.model.AgentMessage;
import com.agent.model.content_block.ToolResultBlock;
import com.agent.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolResultLogHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(LogHook.class);
    @Override
    public HookResult execute(Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];
        ToolResultBlock output = (ToolResultBlock) args[1];
        log.info("[Hook][工具调用结果输出] 调用工具：{}，输入({})，执行结果：{}", block.name()
                , block.input(), output.content());
        return HookResult.proceed();
    }
}
