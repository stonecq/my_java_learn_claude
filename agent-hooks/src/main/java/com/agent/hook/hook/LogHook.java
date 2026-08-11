package com.agent.hook.hook;

import com.agent.core.hook.Hook;
import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookResult;
import com.agent.core.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(LogHook.class);
    private static final int MAX_LOG_LENGTH = 50;
    @Override
    public HookResult execute(HookEvent event, Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];

        String inputStr = String.valueOf(block.input());
        String logInput = inputStr;
        if (inputStr.length() > MAX_LOG_LENGTH) {
            logInput = inputStr.substring(0, MAX_LOG_LENGTH) + "...[truncated]";
        }

        log.info("[Hook][工具调用日志输出] {}({})", block.name(), logInput);
        return HookResult.proceed();
    }
}
