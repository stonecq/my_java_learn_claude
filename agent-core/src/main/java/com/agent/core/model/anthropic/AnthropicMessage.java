package com.agent.core.model.anthropic;

import com.agent.core.enums.RoleEnum;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.MessageConvert;
import com.agent.core.model.content_block.ContentBlock;
import com.agent.core.model.content_block.TextBlock;
import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class AnthropicMessage implements MessageConvert {

    String role;

    List<AnthropicContent> content;

    private static final Map<String, String> AGENT_TO_ANTHROPIC_ROLE_MAP = Map.of(
            RoleEnum.USER.getValue(), "user",
            RoleEnum.ASSISTANT.getValue(), "assistant"
    );
    private static final Map<String, String> ANTHROPIC_TO_AGENT_ROLE_MAP =
            AGENT_TO_ANTHROPIC_ROLE_MAP.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    ));
    public static String toAnthropicRole(String agentRole) {
        String result = AGENT_TO_ANTHROPIC_ROLE_MAP.get(agentRole);
        if (result == null) throw new IllegalArgumentException("非法 role: " + agentRole);
        return result;
    }

    public static String toAgentRole(String anthropicRole) {
        String result = ANTHROPIC_TO_AGENT_ROLE_MAP.get(anthropicRole);
        if (result == null) throw new IllegalArgumentException("非法 role: " + anthropicRole);
        return result;
    }

    @Override
    public void fromAgentMessage(AgentMessage agentMessage) {
        this.role = toAnthropicRole(agentMessage.getRole());
        this.content = agentMessage.getContent().stream()
                .map(this::toAnthropicContent)
                .toList();
    }

    private AnthropicContent toAnthropicContent(ContentBlock block) {
        AnthropicContent anthropicContent = new AnthropicContent();
        switch (block) {
            case TextBlock(String text) -> {
                anthropicContent.setType(AnthropicContentTypeEnum.TEXT.getValue());
                anthropicContent.setText(text);
            }
            case ToolUseBlock(String id, String name, Map<String, Object> input) -> {
                anthropicContent.setType(AnthropicContentTypeEnum.TOOL_USE.getValue());
                anthropicContent.setId(id);
                anthropicContent.setName(name);
                anthropicContent.setInput(input);
            }
            case ToolResultBlock(String id, String content1, Boolean isError) -> {
                anthropicContent.setType(AnthropicContentTypeEnum.TOOL_RESULT.getValue());
                anthropicContent.setToolUseId(id);
                anthropicContent.setContent(content1);
                anthropicContent.setIsError(isError);
            }
            case null, default -> throw new IllegalArgumentException("未知的块类型: " + block);
        }
        return anthropicContent;
    }

    @Override
    public AgentMessage convertToAgentMessage() {
        AgentMessage agentMessage = new AgentMessage();
        String agentRole = toAgentRole(this.role);
        agentMessage.setRole(agentRole);

        List<ContentBlock> contentBlocks = this.content.stream().map(this::toAgentBlock).toList();
        agentMessage.setContent(contentBlocks);
        return agentMessage;
    };

    private ContentBlock toAgentBlock(AnthropicContent anthropicContent){
        if (anthropicContent.getType().equals(AnthropicContentTypeEnum.TEXT.getValue())) {
            return new TextBlock(anthropicContent.getText());
        }else if (anthropicContent.getType().equals(AnthropicContentTypeEnum.TOOL_RESULT.getValue())){
            return new ToolResultBlock(anthropicContent.getToolUseId(), anthropicContent.getContent(), anthropicContent.getIsError());
        }else if (anthropicContent.getType().equals(AnthropicContentTypeEnum.TOOL_USE.getValue())){
            return new ToolUseBlock(anthropicContent.getId(), anthropicContent.getName(), anthropicContent.getInput());
        }else {
            throw new IllegalArgumentException("未知的块类型: " + anthropicContent);
        }
    };

}
