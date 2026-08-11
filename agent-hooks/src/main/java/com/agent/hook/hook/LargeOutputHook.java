package com.agent.hook.hook;

import com.agent.hook.Hook;
import com.agent.hook.HookResult;
import com.agent.model.content_block.ToolResultBlock;
import com.agent.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//大输出警告 Hook
public class LargeOutputHook implements Hook {
    private static final int THRESHOLD = 100_000;
    private static final Logger log = LoggerFactory.getLogger(LargeOutputHook.class);
    @Override
    public HookResult execute(Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];
        ToolResultBlock output = (ToolResultBlock) args[1];
        if (output.content().length() > THRESHOLD){
            log.warn("[HOOK] Large output from {}: {} chars", block.name(), output.content());
        }
        return HookResult.proceed();
    }
}
