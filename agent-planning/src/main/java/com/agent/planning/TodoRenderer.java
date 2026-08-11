package com.agent.planning;

import java.util.List;

// 渲染器
public class TodoRenderer {
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    public String render(List<TodoItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(ANSI_YELLOW).append("## Current Tasks").append(ANSI_RESET).append("\n");
        for (TodoItem item : items) {
            sb.append("  [").append(colorize(item.status())).append("] ")
                    .append(item.content()).append("\n");
        }
        String output = sb.toString();
        System.out.print(output);
        return output;
    }
    private String colorize(TodoStatus status) {
        return switch (status) {
            case PENDING -> " ";
            case IN_PROGRESS -> ANSI_CYAN + status.getValue() + ANSI_RESET;
            case COMPLETED -> ANSI_GREEN + status.getValue() + ANSI_RESET;
        };
    }
}
