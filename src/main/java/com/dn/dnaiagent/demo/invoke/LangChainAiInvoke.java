package com.dn.dnaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/24 23:00
 **/
public class LangChainAiInvoke {
    public static void main(String[] args) {
        QwenChatModel chatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();

        String message = chatModel.chat("你好啊，你是谁呀？");
        System.out.println(message);
    }
}
