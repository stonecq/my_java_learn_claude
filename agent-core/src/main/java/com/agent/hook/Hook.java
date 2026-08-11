package com.agent.hook;

@FunctionalInterface
public interface Hook {
    HookResult execute(Object... args);

    default String getName(){
        return this.getClass().getSimpleName();
    }
}
