package com.agent.hook.hook;

import com.agent.hook.Hook;
import com.agent.hook.HookResult;
import com.agent.model.AgentMessage;
import com.agent.model.content_block.ToolResultBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SummaryHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(SummaryHook.class);

    @Override
    public HookResult execute(Object... args) {
        List<AgentMessage> blocks = (List<AgentMessage>) args[0];
        long toolCount = blocks.stream()
                .flatMap(m -> m.getContent().stream())
                .filter(b -> b instanceof ToolResultBlock).count();
        log.info("[HOOK][总结] Stop: 本次总共调用了 {} 次工具", toolCount);
        return HookResult.proceed();
    }
}
