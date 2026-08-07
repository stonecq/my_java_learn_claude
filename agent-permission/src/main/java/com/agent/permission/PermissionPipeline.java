package com.agent.permission;

import java.util.List;
import java.util.Map;

public class PermissionPipeline {
    private final List<PermissionRule> denyRules;

    private final List<PermissionRule> askRules;

    UserApprovalCallback approvalCallback;

    public PermissionPipeline(List<PermissionRule> denyRules,
                              List<PermissionRule> askRules,
                              UserApprovalCallback approvalCallback) {
        this.denyRules = List.copyOf(denyRules);
        this.askRules = List.copyOf(askRules);
        this.approvalCallback = approvalCallback;
    }


    public PermissionResult check(String toolName, Map<String, Object> input) {
        PermissionResult result = gateOne(toolName, input);
        if (result != null) {
            return result;
        }
        return gateTwo(toolName, input);
    }

    private PermissionResult gateOne(String toolName, Map<String, Object> input) {
        for (PermissionRule rule : denyRules) {
            if (!applies(rule, toolName)) {
                continue;
            }
            PermissionResult result = rule.check(toolName, input);
            if (result instanceof PermissionResult.Denied denied) {
                return denied;
            }
        }
        return null;
    }

    private PermissionResult gateTwo(String toolName, Map<String, Object> input) {
        for (PermissionRule rule : askRules) {
            if (!applies(rule, toolName)) continue;
            switch (rule.check(toolName, input)) {
                case PermissionResult.Denied denied -> { return denied; }
                case PermissionResult.Allowed allowed -> { return allowed; }
                case PermissionResult.Ask ask -> {
                    boolean approved = approvalCallback.ask(toolName, input, ask.reason());
                    return approved ? new PermissionResult.Allowed() : new PermissionResult.Denied("用户拒绝了请求: " + ask.reason());
                }
                case PermissionResult.Passthrough ignored -> {}
            }
        }
        return new PermissionResult.Allowed();
    }

    private boolean applies(PermissionRule rule, String toolName) {
        List<String> applicable = rule.applicableTools();
        return applicable.isEmpty() || applicable.contains(toolName);
    }

}
