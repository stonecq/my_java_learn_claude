package com.agent.core.hook;

@FunctionalInterface
public interface Hook {
    HookResult execute(HookEvent event, Object... args);

    default String getName(){
        return this.getClass().getSimpleName();
    }
}
