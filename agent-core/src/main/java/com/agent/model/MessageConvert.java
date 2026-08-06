package com.agent.model;

public interface MessageConvert {
    void fromAgentMessage(AgentMessage agentMessage);

    AgentMessage convertToAgentMessage();
}
