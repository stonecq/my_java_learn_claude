package com.agent.tool.tools;

import com.agent.tool.Tool;
import com.agent.tool.ToolComponent;
import com.agent.tool.ToolDescriptor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@ToolComponent
public class WriteTool implements Tool {

    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get("path") == null) {
            return "Error: 缺少 'path' 参数";
        }
        if (params.get("content") == null) {
            return "Error: 缺少 'content' 参数";
        }
        String path = (String) params.get("path");
        String content = (String) params.get("content");
        try {
            Path file = Workspace.resolve(path);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return "Wrote " + content.length() + " bytes to " + path;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "write";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "path", Map.of(
                        "type", "string",
                        "description", "工作区内的目标文件路径，父目录不存在时会自动创建"),
                "content", Map.of(
                        "type", "string",
                        "description", "要写入的文件内容")
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("path", "content")
        );
        return new ToolDescriptor(
                getName(),
                "在工作区内写入文本文件（UTF-8），自动创建父目录",
                schema
        );
    }
}
