package com.agent.hook.config;

import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookRegistry;
import com.agent.hook.hook.*;
import com.agent.permission.PermissionConfig;
import com.agent.permission.UserApprovalCallback;
import com.agent.planning.hook.PlanningHook;

import java.nio.file.Path;

public class HookConfig {
    public static HookRegistry withDefault(Path workdir, UserApprovalCallback callback) {
        HookRegistry registry = new HookRegistry();
        registry.register(HookEvent.PRE_TOOL_USE, new LogHook());
        registry.register(HookEvent.PRE_TOOL_USE, new PermissionHook(PermissionConfig.withDefaults(workdir, callback)));
        registry.register(HookEvent.POST_TOOL_USE, new LargeOutputHook());
        registry.register(HookEvent.POST_TOOL_USE, new ToolResultLogHook());
        registry.register(HookEvent.STOP, new SummaryHook());

        PlanningHook planningHook = new PlanningHook();
        registry.register(HookEvent.ROUND_START, planningHook);
        registry.register(HookEvent.POST_TOOL_USE, planningHook, "todo_write");
        return registry;
    }
}
