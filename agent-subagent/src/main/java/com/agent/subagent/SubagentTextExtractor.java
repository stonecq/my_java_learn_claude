package com.agent.subagent;

import com.agent.core.enums.RoleEnum;
import com.agent.core.model.AgentMessage;
import com.agent.core.model.content_block.ContentBlock;
import com.agent.core.model.content_block.TextBlock;

import java.util.List;

public final class SubagentTextExtractor {
    private static final String FALLBACK_MESSAGE = "Subagent stopped without final answer.";

    public static String extract(List<AgentMessage> messages){
        if (messages == null || messages.isEmpty()){
            return FALLBACK_MESSAGE;
        }

        for (int i = messages.size() - 1; i >= 0; i--){
            AgentMessage msg = messages.get(i);
            if (!RoleEnum.ASSISTANT.getValue().equals(msg.getRole())){
                continue;
            }
            String text = extractTextFromContent(msg.getContent());
            if (text != null && !text.isBlank()){
                return text;
            }
        }
        return FALLBACK_MESSAGE;
    }

    private static String extractTextFromContent(List<ContentBlock> content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : content) {
            if (block instanceof TextBlock(String text)) {
                sb.append(text).append("\n");
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }
}
