package com.agent.tool.tools;

import java.nio.file.Path;

/**
 * 工作区路径校验：所有文件类工具只能访问工作区内的文件，
 * 防止路径穿越（../ 或绝对路径）逃逸工作区。
 */
final class Workspace {

    /** 默认取启动目录，可通过 -Dagent.workdir=... 覆盖 */
    private static final Path ROOT = Path.of(
            System.getProperty("agent.workdir", System.getProperty("user.dir")))
            .toAbsolutePath()
            .normalize();

    private Workspace() {
    }

    static Path root() {
        return ROOT;
    }

    static Path resolve(String p) {
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("路径不能为空");
        }
        Path resolved = ROOT.resolve(p).normalize();
        if (!resolved.startsWith(ROOT)) {
            throw new IllegalArgumentException("Path escapes workspace: " + p);
        }
        return resolved;
    }
}
