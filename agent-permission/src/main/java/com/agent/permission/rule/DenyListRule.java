package com.agent.permission.rule;

import com.agent.permission.PermissionResult;
import com.agent.permission.PermissionRule;

import java.util.List;
import java.util.Map;

/**
 * Gate 1: 硬拒绝列表。bash 命令包含任一模式即直接拒绝（大小写不敏感）。
 */
public class DenyListRule implements PermissionRule {

    private final List<String> denyPatterns;

    public DenyListRule(List<String> denyPatterns) {
        this.denyPatterns = List.copyOf(denyPatterns);
    }

    @Override
    public PermissionResult check(String toolName, Map<String, Object> input) {
        Object command = input.get("command");
        if (!(command instanceof String cmd)) {
            return new PermissionResult.Passthrough();
        }
        String lower = cmd.toLowerCase();
        for (String pattern : denyPatterns) {
            if (lower.contains(pattern.toLowerCase())) {
                return new PermissionResult.Denied("被拒绝的命令: " + cmd);
            }
        }
        return new PermissionResult.Passthrough();
    }

    @Override
    public List<String> applicableTools() {
        return List.of("bash");
    }
}
