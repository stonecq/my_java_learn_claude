package com.agent.core.tool.tools;

import java.nio.file.Path;

/**
 * 工作区路径校验：所有文件类工具只能访问工作区内的文件，
 * 防止路径穿越（../ 或绝对路径）逃逸工作区。
 */
final class Workspace {

    private Workspace() {
    }

    /** 默认取启动目录，可通过 -Dagent.workdir=... 覆盖（每次调用读取，便于测试替换） */
    static Path root() {
        return Path.of(System.getProperty("agent.workdir", System.getProperty("user.dir")))
                .toAbsolutePath()
                .normalize();
    }

    static Path resolve(String p) {
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("路径不能为空");
        }
        Path root = root();
        Path resolved = root.resolve(p).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Path escapes workspace: " + p);
        }
        return resolved;
    }
}
