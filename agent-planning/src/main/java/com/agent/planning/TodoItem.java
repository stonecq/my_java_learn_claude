package com.agent.planning;

import java.util.Map;

// 单个Todo项
public record TodoItem(String content, TodoStatus status) {
    public TodoItem{
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content 不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("status 不能为空");
        }
    }

    static public TodoItem fromMap(Map<String, Object> map){
        String content = (String) map.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content 不能为空");
        }
        String statusStr = (String) map.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("status 不能为空");
        }
        return new TodoItem(content, TodoStatus.fromName(statusStr));
    }
}
