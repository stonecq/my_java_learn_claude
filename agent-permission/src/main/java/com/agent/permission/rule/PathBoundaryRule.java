package com.agent.permission.rule;

import com.agent.permission.PermissionResult;
import com.agent.permission.PermissionRule;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Gate 2: 路径边界检查。read/write/edit 的目标路径必须落在工作区内，
 * 用 normalize + startsWith 防止 ../ 或绝对路径逃逸。
 */
public class PathBoundaryRule implements PermissionRule {

    private final Path root;

    public PathBoundaryRule(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public PermissionResult check(String toolName, Map<String, Object> input) {
        Object path = input.get("path");
        if (!(path instanceof String pathStr) || pathStr.isBlank()) {
            return new PermissionResult.Passthrough();
        }
        Path resolved = root.resolve(pathStr).normalize();
        if (!resolved.startsWith(root)) {
            return new PermissionResult.Denied("路径越界: " + pathStr);
        }
        return new PermissionResult.Passthrough();
    }

    @Override
    public List<String> applicableTools() {
        return List.of("read", "write", "edit");
    }
}
