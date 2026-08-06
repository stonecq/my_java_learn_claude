package com.agent.model.anthropic;

import com.agent.enums.StopReasonEnum;
import com.agent.model.AgentResponse;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnthropicResponse {
        public String id;
        public String type;
        public String role;
        public List<AnthropicContent> content;
        public String stopReason;
        public Usage usage;

        public static class Usage {
            public int inputTokens;
            public int outputTokens;

            public AgentResponse.Usage toAgentUsage(){
                AgentResponse.Usage agentUsage = new AgentResponse.Usage();
                agentUsage.setInputTokens(this.inputTokens);
                agentUsage.setOutputTokens(this.outputTokens);
                return agentUsage;
            }
        }

    private static final Map<String, Integer> ANTHROPIC_TO_AGENT_STOP_REASON = Map.of(
            "end_turn",      StopReasonEnum.END_TURN.getValue(),      // 2
            "max_tokens",    StopReasonEnum.MAX_TOKENS.getValue(),    // 3
            "stop_sequence", StopReasonEnum.STOP_SEQUENCE.getValue(), // 4
            "tool_use",      StopReasonEnum.TOOL_USE.getValue()       // 1
    );

    public Integer convertAgentStopReason() {
        if (stopReason == null) {
            return null;
        }
        return ANTHROPIC_TO_AGENT_STOP_REASON.get(stopReason);
    }
    }