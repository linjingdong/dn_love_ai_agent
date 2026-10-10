package com.dn.dnaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/24 22:50
 **/
@Component
public class SpringAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashScopeChatModel;

    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = dashScopeChatModel.call(new Prompt("你好啊，你是谁呀？"))
//                .getResult()
//                .getOutput();
//
//        System.out.println(assistantMessage.getText());
    }
}
