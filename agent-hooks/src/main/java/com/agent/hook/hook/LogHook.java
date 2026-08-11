package com.agent.hook.hook;

import com.agent.hook.Hook;
import com.agent.hook.HookResult;
import com.agent.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(LogHook.class);

    @Override
    public HookResult execute(Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];
        log.info("[Hook][工具调用日志输出] {}({})", block.name(), block.input());
        return HookResult.proceed();
    }
}
