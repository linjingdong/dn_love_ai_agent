package com.dn.dnaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.dn.dnaiagent.agent.model.AgentState;
import com.dn.dnaiagent.exception.BusinessException;
import com.dn.dnaiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。提供状态转换、内存管理和基于步骤的执行循环的基础功能。子类必须实现step方法。
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/19 14:19
 **/
@Data
@Slf4j
public abstract class BaseAgent {
    // 核心属性
    private String name;

    // 提示
    private String SystemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {
        // 1. 基础校验
        if (this.state != AgentState.IDLE) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Cannot run agent from state:" + this.state);
        }
        if (StrUtil.isEmpty(userPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cannot run agent with empty prompt");
        }

        // 2. 执行并更改状态
        state = AgentState.RUNNING;
        // 2.1 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 2.1 保存执行结果
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("Executing step {} / {} ", currentStep, maxSteps);
                String stepResult = this.step();
                String loopResult = "Step " + currentStep + ": " + stepResult;
                results.add(loopResult);
            }

            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps: " + maxSteps);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            cleanup();
        }
    }

    public abstract String step();

    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
