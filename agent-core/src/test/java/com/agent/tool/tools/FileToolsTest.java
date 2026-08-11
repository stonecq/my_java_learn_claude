package com.agent.tool.tools;

import com.agent.core.model.content_block.ToolUseBlock;
import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolDescriptor;
import com.agent.core.tool.ToolRegistry;
import com.agent.core.tool.tools.EditTool;
import com.agent.core.tool.tools.GlobTool;
import com.agent.core.tool.tools.ReadTool;
import com.agent.core.tool.tools.WriteTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileToolsTest {

    private static Path workdir;

    @BeforeAll
    static void init() throws Exception {
        workdir = Files.createTempDirectory("agent-ws");
        System.setProperty("agent.workdir", workdir.toString());
    }

    @Test
    void annotationScan_registersAllFileTools() {
        ToolRegistry registry = new ToolRegistry();
        registry.autoRegisterByAnnotation("com.agent.tool.tools");

        List<String> names = registry.getAllDescriptors().stream()
                .map(ToolDescriptor::getName)
                .sorted()
                .toList();
        assertTrue(names.containsAll(List.of("bash", "read", "write", "edit", "glob")),
                "注解扫描应注册 bash + 四个文件工具，实际: " + names);
    }

    @Test
    void writeThenRead_roundTrip() {
        Tool write = new WriteTool();
        Tool read = new ReadTool();

        String written = write.execute(Map.of(
                "path", "src/hello.txt",
                "content", "第一行\n第二行\n第三行"));
        assertEquals("Wrote 11 bytes to src/hello.txt", written);

        String content = read.execute(Map.of("path", "src/hello.txt"));
        assertEquals("第一行\n第二行\n第三行", content);
    }

    @Test
    void read_limit_truncatesWithNote() {
        Tool write = new WriteTool();
        Tool read = new ReadTool();
        write.execute(Map.of("path", "long.txt", "content", "1\n2\n3\n4\n5\n"));

        String limited = read.execute(Map.of("path", "long.txt", "limit", 2));
        assertEquals("1\n2\n... (3 more lines)", limited);
    }

    @Test
    void edit_replacesOnlyFirstOccurrence() {
        Tool write = new WriteTool();
        Tool edit = new EditTool();
        Tool read = new ReadTool();
        write.execute(Map.of("path", "edit.txt", "content", "foo foo foo"));

        String edited = edit.execute(Map.of(
                "path", "edit.txt", "old_text", "foo", "new_text", "bar"));
        assertEquals("Edited edit.txt", edited);

        assertEquals("bar foo foo", read.execute(Map.of("path", "edit.txt")));
    }

    @Test
    void edit_missingOldText_returnsError() {
        Tool write = new WriteTool();
        Tool edit = new EditTool();
        write.execute(Map.of("path", "edit2.txt", "content", "hello"));

        String result = edit.execute(Map.of(
                "path", "edit2.txt", "old_text", "nope", "new_text", "x"));
        assertTrue(result.startsWith("Error: text not found"), result);
    }

    @Test
    void glob_findsMatchingRelativePaths() {
        Tool write = new WriteTool();
        Tool glob = new GlobTool();
        write.execute(Map.of("path", "a/b/App.java", "content", "class App {}"));
        write.execute(Map.of("path", "a/b/Readme.md", "content", "readme"));

        String result = glob.execute(Map.of("pattern", "**/*.java"));
        assertEquals("a/b/App.java", result);
    }

    @Test
    void pathTraversal_isBlocked() {
        Tool read = new ReadTool();
        Tool write = new WriteTool();

        String escapeRead = read.execute(Map.of("path", "../../../etc/hosts"));
        assertTrue(escapeRead.startsWith("Error: Path escapes workspace"), escapeRead);

        String escapeWrite = write.execute(Map.of(
                "path", "../evil.txt", "content", "hack"));
        assertTrue(escapeWrite.startsWith("Error: Path escapes workspace"), escapeWrite);
    }

    @Test
    void dispatchViaRegistry_runsToolAndReturnsResult() throws Exception {
        ToolRegistry registry = new ToolRegistry();
        registry.autoRegisterByAnnotation("com.agent.tool.tools");

        var result = registry.dispatch(new ToolUseBlock("t1", "write",
                Map.of("path", "via-registry.txt", "content", "ok")));
        assertNotNull(result);
        assertTrue(result.content().contains("Wrote"), result.content());
        assertEquals("ok", Files.readString(workdir.resolve("via-registry.txt")));
    }
}
