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
public class EditTool implements Tool {

    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get("path") == null) {
            return "Error: 缺少 'path' 参数";
        }
        if (params.get("old_text") == null) {
            return "Error: 缺少 'old_text' 参数";
        }
        if (params.get("new_text") == null) {
            return "Error: 缺少 'new_text' 参数";
        }
        String path = (String) params.get("path");
        String oldText = (String) params.get("old_text");
        String newText = (String) params.get("new_text");
        try {
            Path file = Workspace.resolve(path);
            if (!Files.exists(file)) {
                return "Error: 文件不存在: " + path;
            }
            String text = Files.readString(file, StandardCharsets.UTF_8);
            int index = text.indexOf(oldText);
            if (index < 0) {
                return "Error: text not found in " + path;
            }
            String edited = text.substring(0, index) + newText
                    + text.substring(index + oldText.length());
            Files.writeString(file, edited, StandardCharsets.UTF_8);
            return "Edited " + path;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "path", Map.of(
                        "type", "string",
                        "description", "工作区内的目标文件路径"),
                "old_text", Map.of(
                        "type", "string",
                        "description", "要查找的旧文本（只替换第一次出现的匹配）"),
                "new_text", Map.of(
                        "type", "string",
                        "description", "替换后的新文本")
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("path", "old_text", "new_text")
        );
        return new ToolDescriptor(
                getName(),
                "在工作区内的文本文件中替换第一处匹配的文本（基于字符串匹配，非正则）",
                schema
        );
    }
}
