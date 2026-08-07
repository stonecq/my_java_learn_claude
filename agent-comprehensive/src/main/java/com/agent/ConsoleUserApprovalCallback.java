package com.agent;

import com.agent.permission.UserApprovalCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 控制台版用户审批回调：在终端打印询问并读取用户输入。
 * 由 CLI 入口注入 PermissionPipeline，权限模块本身不碰 System.in。
 */
public class ConsoleUserApprovalCallback implements UserApprovalCallback {

    private final BufferedReader reader;

    public ConsoleUserApprovalCallback() {
        this(new BufferedReader(new InputStreamReader(System.in)));
    }

    ConsoleUserApprovalCallback(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean ask(String toolName, Map<String, Object> input, String reason) {
        System.out.println("\n[权限确认] 工具: " + toolName + " | 原因: " + reason);
        System.out.print("允许执行吗? (y/n): ");
        try {
            String line = reader.readLine();
            if (line == null) {
                return false;
            }
            String trimmed = line.trim().toLowerCase();
            return trimmed.startsWith("y");
        } catch (IOException e) {
            return false;
        }
    }
}
