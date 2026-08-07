package com.agent.permission;

import java.util.List;
import java.util.Map;

public interface PermissionRule {
    PermissionResult check(String toolName, Map<String, Object> input);

    // （空列表表示适用所有工具）
    default List<String> applicableTools(){
        return List.of();
    }
}
