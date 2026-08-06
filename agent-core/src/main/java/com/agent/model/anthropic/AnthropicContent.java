package com.agent.model.anthropic;

import lombok.Data;

import java.util.Map;

@Data
public class AnthropicContent{
        private String type;

        /**************   text   ***************/
        private String text;

        /************** tool use ***************/
        private String id;

        private String name;

        private Map<String, Object> input;

        /************** tool result ***************/
        private String toolUseId;

        private String content;

        private Boolean isError;

}