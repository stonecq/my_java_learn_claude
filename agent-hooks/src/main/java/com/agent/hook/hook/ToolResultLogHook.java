package com.agent.hook.hook;

import com.agent.core.hook.Hook;
import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookResult;
import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.agent.core.utils.StringUtils.truncate;

public class ToolResultLogHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(ToolResultLogHook.class);
    private static final int MAX_LOG_LENGTH = 50;

    @Override
    public HookResult execute(HookEvent event, Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];
        ToolResultBlock output = (ToolResultBlock) args[1];
        String inputStr = String.valueOf(block.input());
        String logInput = truncate(inputStr, MAX_LOG_LENGTH);
        String outputStr = String.valueOf(output.content());
        String logOutput = truncate(outputStr, MAX_LOG_LENGTH);

        log.info("[Hook][工具调用结果输出] 调用工具：{}，输入({})，执行结果：{}",
                block.name(), logInput, logOutput);

        return HookResult.proceed();
    }


}