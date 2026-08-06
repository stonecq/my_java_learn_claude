package com.agent.exception;

public class LLMClientException extends RuntimeException {
    public LLMClientException(String message) {
        super(message);
    }
    public LLMClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
