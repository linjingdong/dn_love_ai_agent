package com.dn.dnaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.dn.dnaiagent.agent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了think和act方法，可以用作创建父类的身体
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/19 14:20
 **/
@Slf4j
public class ToolCallAgent extends ReActAgent {
    // 可用的工具调用
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    // 工具调用管理类
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己来维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder()
                .build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public boolean think() {
        // 1. 校验提示词，拼接用户提示词
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        try {
            // 2. 调用 AI 大模型，获取工具调用结果
            Prompt prompt = new Prompt(getMessageList(), chatOptions);
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于act
            this.toolCallChatResponse = chatResponse;

            // 3. 解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 要调用的工具
            List<AssistantMessage.ToolCall> toolCallList = chatResponse.getResult().getOutput().getToolCalls();
            // 要调用的工具详情
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            // 输出提示信息
            log.info("{} 的思考：{} ", getName(), assistantMessage.getText());
            log.info("{} 选择了：{} 个工具来使用", getName(), toolCallList.size());
            log.info(toolCallInfo);

            // 4. 返回结果
            if (toolCallList.isEmpty()) {
                // 只有不调用工具的时候，才会记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 调用工具的时候，不需要记录助手消息，因为调用工具的时候会自动记录
                return true;
            }
        } catch (Exception e) {
            // 异常处理
            log.error("{} 的思考过程当中出现了问题：{}", getName(), e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!this.toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, this.toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 判断是否调用终止工具
        boolean isTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> "doTerminate".equals(toolResponse.name()));
        if (isTerminate) {
            setState(AgentState.FINISHED);
        }

        String toolCallResults = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> String.format("工具 %s 返回的结果 %s", toolResponse.name(), toolResponse.responseData()))
                .collect(Collectors.joining("\n"));
        log.info(toolCallResults);
        return toolCallResults;
    }
}
