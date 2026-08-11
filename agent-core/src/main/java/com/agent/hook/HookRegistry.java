package com.agent.hook;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class HookRegistry {
    private final Map<HookEvent, List<Hook>> register = new EnumMap<>(HookEvent.class);


    public HookRegistry(){
        for (HookEvent event : HookEvent.values()){
            register.put(event, new ArrayList<>());
        }
    }

    public void register(HookEvent event, Hook hook){
        register.get(event).add(hook);
    }

    public HookResult trigger(HookEvent event, Object... args){
        for (Hook hook : register.get(event)){
            HookResult result = hook.execute(args);
            switch (result){
                case HookResult.Blocked blocked -> {
                    return  blocked;
                }
                case HookResult.Cancelled cancelled -> {
                    return cancelled;
                }
                case HookResult.ForceContinue forceContinue -> {
                    return  forceContinue;
                }
                case HookResult.Proceed proceed -> {
                }
            }
        }
        return HookResult.proceed();
    }
}
