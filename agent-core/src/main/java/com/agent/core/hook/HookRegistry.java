package com.agent.core.hook;

import com.agent.core.model.content_block.ToolUseBlock;

import javax.annotation.Nullable;
import java.util.*;

public class HookRegistry {
    // 全局注册表
    private final Map<HookEvent, List<Hook>> wholeRegister = new EnumMap<>(HookEvent.class);
    // 按工具的注册表
    private final Map<HookEvent, Map<String,  List<Hook>>> toolHookRegister = new EnumMap<>(HookEvent.class);


    public HookRegistry(){
        for (HookEvent event : HookEvent.values()){
            wholeRegister.put(event, new ArrayList<>());
            toolHookRegister.put(event, new HashMap<>());
        }
    }

    public void register(HookEvent event, Hook hook){
        wholeRegister.get(event).add(hook);
    }

    public void register(HookEvent event, Hook hook, String toolName){
        List<HookEvent> hookEvents = List.of(HookEvent.POST_TOOL_USE,  HookEvent.PRE_TOOL_USE);
        if (!hookEvents.contains(event)){
            throw new RuntimeException("不支持的HookEvent类型，仅支持" + hookEvents);
        }
        Map<String, List<Hook>> toolHooksMap = toolHookRegister.get(event);
        List<Hook> hookList = toolHooksMap.computeIfAbsent(toolName, k -> new ArrayList<>());
        hookList.add(hook);
    }

    public HookResult trigger(HookEvent event, Object... args){
        for (Hook hook : wholeRegister.get(event)){
            HookResult result = hook.execute(event, args);
            var blocked = getRecord(result);
            if (blocked != null) return blocked;
        }
        Map<String, List<Hook>> toolHookMap = toolHookRegister.get(event);
        if (!toolHookMap.isEmpty()){
            ToolUseBlock block = (ToolUseBlock) args[0];
            for (Hook hook : toolHookMap.getOrDefault(block.name(), List.of())){
                HookResult result = hook.execute(event, args);
                var blocked = getRecord(result);
                if (blocked != null) return blocked;
            }
        }
        return HookResult.proceed();
    }

    @Nullable
    private static HookResult getRecord(HookResult result) {
        switch (result){
            case HookResult.Blocked blocked -> {
                return blocked;
            }
            case HookResult.Cancelled cancelled -> {
                return cancelled;
            }
            case HookResult.ForceContinue forceContinue -> {
                return forceContinue;
            }
            case HookResult.Proceed proceed -> {
            }
        }
        return null;
    }
}
