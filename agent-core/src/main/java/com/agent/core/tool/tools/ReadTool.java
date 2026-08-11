package com.agent.core.tool.tools;

import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolComponent;
import com.agent.core.tool.ToolDescriptor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToolComponent
public class ReadTool implements Tool {

    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get("path") == null) {
            return "Error: 缺少 'path' 参数";
        }
        String path = (String) params.get("path");
        Integer limit = params.get("limit") instanceof Number n ? n.intValue() : null;
        try {
            Path file = Workspace.resolve(path);
            if (!Files.exists(file)) {
                return "Error: 文件不存在: " + path;
            }
            if (!Files.isRegularFile(file)) {
                return "Error: 不是文件: " + path;
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (limit != null && limit > 0 && limit < lines.size()) {
                int more = lines.size() - limit;
                lines = new ArrayList<>(lines.subList(0, limit));
                lines.add("... (" + more + " more lines)");
            }
            String content = String.join("\n", lines);
            if (content.isEmpty()) {
                return "(空文件)";
            }
            return content;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "read";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "path", Map.of(
                        "type", "string",
                        "description", "工作区内的文件路径（例如：src/main/java/App.java）"),
                "limit", Map.of(
                        "type", "integer",
                        "description", "最多读取的行数，不传则读取全部")
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("path")
        );
        return new ToolDescriptor(
                getName(),
                "读取工作区内的文本文件内容（UTF-8），返回文件内容或错误信息",
                schema
        );
    }
}
