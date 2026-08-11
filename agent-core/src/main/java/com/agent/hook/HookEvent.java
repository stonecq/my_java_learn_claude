package com.agent.hook;

import lombok.Getter;

@Getter
public enum HookEvent {
    USER_PROMPT_SUBMIT("UserPromptSubmit"),
    PRE_TOOL_USE("PreToolUse"),
    POST_TOOL_USE("PostToolUse"),
    STOP("Stop");

    private final String label;
    HookEvent(String label) { this.label = label; }
}
