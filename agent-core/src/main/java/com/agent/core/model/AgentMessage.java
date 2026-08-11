package com.agent.core.model;

import com.agent.core.enums.RoleEnum;
import com.agent.core.model.content_block.ContentBlock;
import com.agent.core.model.content_block.TextBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/*
    对话内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentMessage {
    String role;

    List<ContentBlock> content;

    public static AgentMessage forUser(String msg){
        ContentBlock contentBlock = new TextBlock(msg);
        return new AgentMessage(RoleEnum.USER.getValue(), List.of(contentBlock));
    }

    public static AgentMessage forAssistant(List<ContentBlock> blocks){
        return new AgentMessage(RoleEnum.ASSISTANT.getValue(), blocks);
    }

    public static AgentMessage forToolResults(List<ContentBlock> blocks){
        return new AgentMessage(RoleEnum.USER.getValue(), blocks);
    }
}
