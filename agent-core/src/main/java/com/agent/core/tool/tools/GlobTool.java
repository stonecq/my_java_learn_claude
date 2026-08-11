package com.agent.core.tool.tools;

import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolComponent;
import com.agent.core.tool.ToolDescriptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ToolComponent
public class GlobTool implements Tool {

    private static final int MAX_RESULTS = 200;

    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get("pattern") == null) {
            return "Error: 缺少 'pattern' 参数";
        }
        String pattern = (String) params.get("pattern");
        try {
            Pattern regex = globToRegex(pattern);
            Path root = Workspace.root();
            List<String> matches = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(root)) {
                stream.forEach(path -> {
                    String relative = root.relativize(path).toString().replace('\\', '/');
                    if (regex.matcher(relative).matches()) {
                        matches.add(relative);
                    }
                });
            }
            matches.sort(Comparator.naturalOrder());
            if (matches.isEmpty()) {
                return "(no matches)";
            }
            if (matches.size() > MAX_RESULTS) {
                List<String> head = new ArrayList<>(matches.subList(0, MAX_RESULTS));
                head.add("... (" + (matches.size() - MAX_RESULTS) + " more)");
                return String.join("\n", head);
            }
            return String.join("\n", matches);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 把 glob 模式（支持 **、*、?）转成正则，路径统一用 / 分隔以跨平台匹配。
     */
    private static Pattern globToRegex(String glob) {
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                    sb.append(".*");
                    i++;
                } else {
                    sb.append("[^/]*");
                }
            } else if (c == '?') {
                sb.append("[^/]");
            } else if ("\\.^$|(){}[]+".indexOf(c) >= 0) {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return Pattern.compile(sb.append('$').toString());
    }

    @Override
    public String getName() {
        return "glob";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "pattern", Map.of(
                        "type", "string",
                        "description", "glob 模式（例如：**/*.java 或 src/**/pom.xml）")
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("pattern")
        );
        return new ToolDescriptor(
                getName(),
                "在工作区内按 glob 模式查找文件/目录，返回匹配到的相对路径列表",
                schema
        );
    }
}
