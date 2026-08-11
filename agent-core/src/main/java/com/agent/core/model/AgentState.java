package com.agent.core.model;

import lombok.Data;

import java.util.List;

@Data
public class AgentState {
    List<AgentMessage> messages;
    Integer turnCount;

}
