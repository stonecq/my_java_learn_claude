package com.agent.tool.tools;

import com.agent.exception.ToolException;
import com.agent.tool.Tool;
import com.agent.tool.ToolComponent;
import com.agent.tool.ToolDescriptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ToolComponent
public class BashTool implements Tool {
    private static final String SHELL;
    private static final String SHELL_FLAG;
    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows 环境：使用 CMD
            SHELL = "cmd";
            SHELL_FLAG = "/c";
        } else {
            // Linux / macOS / Unix：使用 bash
            SHELL = "bash";
            SHELL_FLAG = "-c";
        }
    }
    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || !params.containsKey("command")) {
            return "Error: 缺少 'command' 参数";
        }
        String command = (String) params.get("command");
        if (command == null || command.isBlank()) {
            return "Error: 命令不能为空";
        }
        if (!isCommandAllowed(command)) {
            return "Error: 命令被安全策略禁止";
        }

        try {
            // 关键修改：使用动态的 SHELL 和 SHELL_FLAG，而非硬编码 "bash"
            ProcessBuilder processBuilder = new ProcessBuilder(SHELL, SHELL_FLAG, command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: 命令执行超时（超过 30 秒）";
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            String result = output.toString().trim();
            return result.isEmpty() ? "命令执行成功（无输出内容）" : result;

        } catch (Exception e) {
            return "Error: 执行命令时发生异常 - " + e.getMessage();
        }
    }

    private boolean isCommandAllowed(String command) {
        String lower = command.toLowerCase();
        if (lower.contains("rm") && lower.contains("-rf")) return false;
        if (lower.contains("curl") && lower.contains("| bash")) return false;
        if (lower.contains("wget") && lower.contains("| bash")) return false;
        if (lower.contains("shutdown") || lower.contains("reboot")) return false;
        // 放行基本命令（你可以添加更多）
        return lower.startsWith("echo") || lower.startsWith("ls") ||
                lower.startsWith("cat") || lower.startsWith("pwd") ||
                lower.startsWith("date") || lower.startsWith("whoami");
    }

    @Override
    public String getName() {
        return "bash";
    }

    @Override
    public ToolDescriptor getDescriptor() {
        Map<String, Object> properties = Map.of(
                "command", Map.of(
                        "type", "string",
                        "description", "要执行的 Bash 命令（例如：ls -la）"
                )
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("command")
        );
        return new ToolDescriptor(
                getName(),
                "在本地 Shell 环境中执行 Bash 命令并返回标准输出和错误",
                schema
        );
    }

}
