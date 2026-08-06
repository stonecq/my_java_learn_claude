package com.agent;

import com.agent.client.DeepSeekLLMClient;
import com.agent.engine.AgentEngine;
import com.agent.model.AgentResponse;
import com.agent.model.content_block.ContentBlock;
import com.agent.model.content_block.TextBlock;
import com.agent.tool.ToolRegistry;
import com.agent.tool.tools.BashTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AgentHarnessApplication {

    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final String SYSTEM_PROMPT = "你是一个本地智能体助手，可以通过 bash 工具在用户的电脑上执行命令。回答用中文。";
    private static final int MAX_TOKENS = 2048;

    public static void main(String[] args) throws IOException {
        // 1. 读取配置：API Key 与 Model ID 从环境变量获取
        String apiKey = "sk-2349ae0c5fdc4fa6afac0bc9d0075860";
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("缺少环境变量 DEEPSEEK_API_KEY，请先设置后再运行。");
            System.err.println("示例（PowerShell）：$env:DEEPSEEK_API_KEY = \"sk-xxx\"");
            System.exit(1);
            return;
        }

        String model = System.getenv("DEEPSEEK_MODEL");
        if (model == null || model.isBlank()) {
            model = DEFAULT_MODEL;
        }

        // 2. 组装 AgentEngine：Client + ToolRegistry(BashTool) + Engine
        DeepSeekLLMClient client = new DeepSeekLLMClient(apiKey, model);
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.autoRegisterByAnnotation("com.agent.tool.tools");
        AgentEngine engine = new AgentEngine(client, toolRegistry);

        System.out.println("Agent 已就绪（model: " + model + "）。输入问题开始对话，输入 exit/quit 退出。");

        // 3. 用户交互：读取输入 -> 调用引擎 -> 输出结果
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("\n> ");
            String query = reader.readLine();
            if (query == null) {
                break;
            }
            if (query.isBlank()) {
                continue;
            }
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                break;
            }

            try {
                AgentResponse response = engine.run(SYSTEM_PROMPT, query, MAX_TOKENS);
                printFinalAnswer(response);
            } catch (Exception e) {
                System.err.println("出错: " + e.getMessage());
            }
        }
        System.out.println("\n再见！");
    }

    /**
     * 最终回答以文本块形式返回；工具调用过程对用户透明，只打印文本。
     */
    private static void printFinalAnswer(AgentResponse response) {
        for (ContentBlock block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                System.out.println(textBlock.text());
            }
        }
    }
}
