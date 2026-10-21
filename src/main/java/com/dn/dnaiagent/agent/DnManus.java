package com.dn.dnaiagent.agent;

import com.dn.dnaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体，拥有自主规划能力，可以直接使用
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/19 14:20
 **/
@Component
public class DnManus extends ToolCallAgent {
    private final static String SYSTEM_PROMPT = """  
            You are DnManus, an all-capable AI assistant, aimed at solving any task presented by the user.
            You have various tools at your disposal that you can call upon to efficiently complete complex requests.
            """;
    private final static String NEXT_STEP_PROMPT = """  
            Based on user needs, proactively select the most appropriate tool or combination of tools.
            For complex tasks, you can break down the problem and use different tools step by step to solve it.
            After using each tool, clearly explain the execution results and suggest the next steps.
            If you want to stop the interaction at any point, use the `terminate` tool/function call.
            """;

    public DnManus(ToolCallback[] allTools, ChatModel dashScopeChatModel) {
        super(allTools);
        this.setName("DnManus");
        this.setSystemPrompt(SYSTEM_PROMPT);
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        this.setChatClient(ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build());
    }
}
