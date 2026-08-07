package com.agent.permission;

import com.agent.permission.rule.DenyListRule;
import com.agent.permission.rule.DestructiveCommandRule;
import com.agent.permission.rule.PathBoundaryRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionPipelineTest {

    @TempDir
    Path tempDir;

    @Test
    void denyRule_blocksTool() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(new DenyListRule(List.of("rm -rf /"))),
                List.of(),
                (name, input, reason) -> true);

        PermissionResult result = pipeline.check("bash", Map.of("command", "rm -rf /usr"));
        assertTrue(result.isDenied(), "Gate 1 deny 命中应直接拦截");
    }

    @Test
    void noRuleMatch_returnsAllowed() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(new DenyListRule(List.of("rm -rf /"))),
                List.of(),
                (name, input, reason) -> true);

        PermissionResult result = pipeline.check("bash", Map.of("command", "echo hello"));
        assertTrue(result.isAllowed(), "未命中任何规则应放行");
    }

    @Test
    void destructiveCommand_approved_allows() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(),
                List.of(new DestructiveCommandRule(List.of("git push --force"))),
                (name, input, reason) -> true);

        PermissionResult result = pipeline.check("bash", Map.of("command", "git push --force origin main"));
        assertTrue(result.isAllowed(), "用户批准破坏性命令应放行");
    }

    @Test
    void destructiveCommand_rejected_denies() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(),
                List.of(new DestructiveCommandRule(List.of("git push --force"))),
                (name, input, reason) -> false);

        PermissionResult result = pipeline.check("bash", Map.of("command", "git push --force origin main"));
        assertTrue(result.isDenied(), "用户拒绝破坏性命令应拦截");
    }

    @Test
    void pathBoundary_deniesEscape() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(),
                List.of(new PathBoundaryRule(tempDir)),
                (name, input, reason) -> true);

        assertTrue(pipeline.check("write", Map.of("path", "../outside.txt")).isDenied(), "越界路径应被拒绝");
        assertFalse(pipeline.check("write", Map.of("path", "a/b.txt")).isDenied(), "工作区内的路径应放行");
    }

    @Test
    void rule_appliesOnlyToConfiguredTools() {
        PermissionPipeline pipeline = new PermissionPipeline(
                List.of(new DenyListRule(List.of("rm -rf /"))),
                List.of(),
                (name, input, reason) -> true);

        PermissionResult result = pipeline.check("write", Map.of("path", "rm -rf /"));
        assertFalse(result.isDenied(), "bash 专用 deny 规则不应拦截其他工具");
    }
}
