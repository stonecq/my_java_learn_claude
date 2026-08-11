package com.agent.core.exception;

public class ToolException extends RuntimeException {

    // 1. 无参构造函数
    public ToolException() {
        super();
    }

    // 2. 仅接收错误消息的构造函数
    public ToolException(String message) {
        super(message);
    }

    // 3. 仅接收原始异常的构造函数
    public ToolException(Throwable cause) {
        super(cause);
    }

    // 4. 同时接收错误消息和原始异常的构造函数 (最常用)
    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
}