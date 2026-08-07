package com.agent.permission.rule;

import com.agent.permission.PermissionResult;
import com.agent.permission.PermissionRule;

import java.util.List;
import java.util.Map;

/**
 * Gate 2: 破坏性命令检查。命中破坏性模式时不是直接拒绝，而是请求用户确认。
 */
public class DestructiveCommandRule implements PermissionRule {

    private final List<String> destructivePatterns;

    public DestructiveCommandRule(List<String> destructivePatterns) {
        this.destructivePatterns = List.copyOf(destructivePatterns);
    }

    @Override
    public PermissionResult check(String toolName, Map<String, Object> input) {
        Object command = input.get("command");
        if (!(command instanceof String cmd)) {
            return new PermissionResult.Passthrough();
        }
        String lower = cmd.toLowerCase();
        for (String pattern : destructivePatterns) {
            if (lower.contains(pattern.toLowerCase())) {
                return new PermissionResult.Ask("破坏性命令，请确认: " + cmd);
            }
        }
        return new PermissionResult.Passthrough();
    }

    @Override
    public List<String> applicableTools() {
        return List.of("bash");
    }
}
