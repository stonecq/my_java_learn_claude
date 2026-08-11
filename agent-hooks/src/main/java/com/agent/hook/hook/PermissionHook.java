package com.agent.hook.hook;

import com.agent.core.hook.Hook;
import com.agent.core.hook.HookEvent;
import com.agent.core.hook.HookResult;
import com.agent.core.model.content_block.ToolUseBlock;
import com.agent.permission.PermissionPipeline;
import com.agent.permission.PermissionResult;

public class PermissionHook implements Hook {
    private final PermissionPipeline pipeline;

    public PermissionHook(PermissionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public HookResult execute(HookEvent event, Object... args) {
        ToolUseBlock block = (ToolUseBlock) args[0];
        PermissionResult result = pipeline.check(block.name(), block.input());
        return switch (result) {
            case PermissionResult.Denied(String reason) -> HookResult.blocked(reason);
            case PermissionResult.Allowed() -> HookResult.proceed();
            case PermissionResult.Ask(String reason) -> HookResult.blocked(reason);
            case PermissionResult.Passthrough() -> HookResult.proceed();
        };
    }
}
