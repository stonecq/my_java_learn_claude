package com.agent.permission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionConfigTest {

    @TempDir
    Path tempDir;

    private static PermissionResult bash(PermissionPipeline pipeline, String command) {
        return pipeline.check("bash", Map.of("command", command));
    }

    @Test
    void withDefaults_buildsDenyAndAskRules() {
        PermissionPipeline pipeline = PermissionConfig.withDefaults(tempDir, (name, input, reason) -> true);

        assertTrue(bash(pipeline, "rm -rf /usr").isDenied(), "Gate 1 deny 规则应生效");
        assertTrue(bash(pipeline, "git push --force origin main").isAllowed(), "Gate 2 ask 规则批准后应放行");

        PermissionResult escape = pipeline.check("write", Map.of("path", "../outside.txt"));
        assertTrue(escape.isDenied(), "路径越界应被拒绝");
    }

    @Test
    void withDefaults_blocksAllStandardDangerousOperations() {
        PermissionPipeline pipeline = PermissionConfig.withDefaults(tempDir, (name, input, reason) -> true);

        List<String> dangerous = List.of(
                "rm -rf /", "sudo apt install x", "shutdown now", "reboot",
                "mkfs.ext4 /dev/sdb1", "dd if=/dev/zero of=/dev/sda", "echo hi > /dev/sda");
        for (String cmd : dangerous) {
            assertTrue(bash(pipeline, cmd).isDenied(), "默认配置应拦截: " + cmd);
        }
    }

    @Test
    void withDefaults_allowsNormalCommands() {
        PermissionPipeline pipeline = PermissionConfig.withDefaults(tempDir, (name, input, reason) -> true);

        assertFalse(bash(pipeline, "echo hello").isDenied(), "普通命令应放行");
    }
}
