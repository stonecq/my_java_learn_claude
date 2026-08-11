package com.agent.core.client;

import com.agent.core.enums.RoleEnum;
import com.agent.core.exception.LLMClientException;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.AgentResponse;
import com.agent.core.model.anthropic.*;
import com.agent.core.model.content_block.ContentBlock;
import com.agent.core.model.content_block.TextBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import com.agent.core.tool.ToolDescriptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeepSeekLLMClient implements Client {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/anthropic/v1/messages";

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekLLMClient(String apiKey, String model){
        this(apiKey, model, DEFAULT_BASE_URL);
    };

    DeepSeekLLMClient(String apiKey, String model, String baseUrl){
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // Anthropic API 使用 snake_case
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();

    }

    @Override
    public AgentResponse sendMessage(String system, List<AgentMessage> messages, List<ToolDescriptor> tools, int maxTokens) throws LLMClientException {
        AnthropicRequest requestBody = buildAnthropicRequest(system, messages, tools, maxTokens);
        try{
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. 处理 HTTP 状态码
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                // 成功响应，解析为 AgentResponse
                return parseResponse(response.body());
            } else {
                // 错误处理
                handleErrorResponse(statusCode, response.body());
                throw new LLMClientException("Unexpected error"); // 不会被触达
            }

        }catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMClientException("HTTP request failed", e);
        } catch (Exception e) {
            throw new LLMClientException("Failed to process request", e);
        }
    }
    private AgentResponse parseResponse(String jsonResponse) throws IOException {
        // 反序列化为 AnthropicResponse 对象
        AnthropicResponse apiResponse = objectMapper.readValue(jsonResponse, AnthropicResponse.class);

        // 构造 AgentMessage（assistant 角色）
        List<ContentBlock> blocks = new ArrayList<>();
        for (AnthropicContent content : apiResponse.content) {
            if (AnthropicContentTypeEnum.TEXT.getValue().equals(content.getType())) {
                blocks.add(new TextBlock(content.getText()));
            } else if (AnthropicContentTypeEnum.TOOL_USE.getValue().equals(content.getType())) {
                String id = content.getId();
                blocks.add(new ToolUseBlock(id, content.getName(), content.getInput()));
            }
        }

        AgentMessage assistantMsg = new AgentMessage(RoleEnum.ASSISTANT.getValue(), blocks);


        AgentResponse response = new AgentResponse();

        response.setStopReason(apiResponse.convertAgentStopReason());
        response.setContent(assistantMsg.getContent());
        response.setUsage(apiResponse.getUsage().toAgentUsage());
        response.setModel(this.model);
        return response;
    }


    private void handleErrorResponse(int statusCode, String responseBody) throws LLMClientException {
        String errorMsg;
        try {
            // Anthropic 错误格式为 {"error": {"type": "...", "message": "..."}}
            Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
            Object error = errorMap.get("error");
            if (error instanceof Map) {
                Object message = ((Map<?, ?>) error).get("message");
                errorMsg = message != null ? message.toString() : error.toString();
            } else if (error != null) {
                errorMsg = error.toString();
            } else {
                errorMsg = responseBody;
            }
        } catch (Exception e) {
            errorMsg = responseBody;
        }

        String statusMessage = switch (statusCode) {
            case 400 -> "Bad Request - 参数错误";
            case 429 -> "Rate Limit - 请求过于频繁，请稍后重试";
            case 500 -> "Internal Server Error - 服务器错误";
            default -> "HTTP " + statusCode;
        };
        throw new LLMClientException(statusMessage + ": " + errorMsg);
    }

    private AnthropicRequest buildAnthropicRequest(String system, List<AgentMessage> messages, List<ToolDescriptor> tools, int maxTokens) {
        AnthropicRequest request = new AnthropicRequest();
        List<AnthropicMessage> anthropicMessageList = messages.stream().map(agentMessage -> {
            AnthropicMessage anthropicMessage = new AnthropicMessage();
            anthropicMessage.fromAgentMessage(agentMessage);
            return anthropicMessage;
        }).toList();
        request.setModel(this.model);
        request.setSystem(system);
        request.setMessages(anthropicMessageList);
        request.setMaxTokens(maxTokens);
        request.setTools(tools.stream().map(AnthropicTool::from).toList());
        return request;
    }
}
