package com.agent.core.model;

public interface MessageConvert {
    void fromAgentMessage(AgentMessage agentMessage);

    AgentMessage convertToAgentMessage();
}
