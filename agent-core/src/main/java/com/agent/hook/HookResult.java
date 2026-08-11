package com.agent.hook;

public sealed interface HookResult {
    // 放行，继续执行
    record Proceed() implements HookResult {}
    // 阻止本次工具调用
    record Blocked(String reason) implements HookResult {}
    // 阻止退出
    record ForceContinue(String message) implements HookResult {}
    // 取消
    record Cancelled() implements HookResult {}

    static HookResult proceed() { return new Proceed(); }
    static HookResult blocked(String reason) { return new Blocked(reason); }
    static HookResult forceContinue(String msg) { return new ForceContinue(msg); }
    static HookResult cancelled() { return new Cancelled(); }

    default boolean isBlocked() { return this instanceof Blocked; }
    default boolean isForceContinue() { return this instanceof ForceContinue; }

}
