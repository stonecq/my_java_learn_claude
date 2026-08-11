package com.agent.planning.hook;

import com.agent.core.hook.Hook;
import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookResult;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlanningHook implements Hook {
    private static final Logger log = LoggerFactory.getLogger(PlanningHook.class);
    private static final int NAG_THRESHOLD = 3;
    private static final String REMINDER_MESSAGE = "<reminder>Update your todos.</reminder>";

    private int roundsSinceTodo = 0;
    @Override
    public HookResult execute(HookEvent event, Object... args) {
        switch (event) {
            case ROUND_START -> {
                // 查询/注入:每轮
                roundsSinceTodo++;
                if (shouldNag()) {
                    List<AgentMessage> messages = (List<AgentMessage>) args[0];
                    messages.add(AgentMessage.forUser(REMINDER_MESSAGE));
                }
                return HookResult.proceed();
            }
            case POST_TOOL_USE -> {        // 重置:todo_write 之后
                roundsSinceTodo = 0;       // 这个分支才需要强转 args[0]/args[1]
                ToolUseBlock block = (ToolUseBlock) args[0];
                ToolResultBlock resultBlock = (ToolResultBlock) args[1];

                return HookResult.proceed();
            }
            default ->  {
                return HookResult.proceed();
            }
        }
    }

    private boolean shouldNag() {
        return roundsSinceTodo >= NAG_THRESHOLD;
    }
}
