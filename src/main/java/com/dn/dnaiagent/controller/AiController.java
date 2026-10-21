package com.dn.dnaiagent.controller;

import com.dn.dnaiagent.agent.DnManus;
import com.dn.dnaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/21 16:34
 **/
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashScopeChatModel;

    /**
     * 同步与 AI 恋爱大师对话
     * @param message 消息
     * @param chatId 对话id
     * @return 响应
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * 通过 SSE 模式与 AI 恋爱大师对话
     * @param message 消息
     * @param chatId 对话id
     * @return 响应
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 通过 SSE 模式与 AI 恋爱大师对话 (通过ServerSentEvent返回)
     * @param message 消息
     * @param chatId 对话id
     * @return 响应
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 通过 SSE 模式与 AI 恋爱大师对话 （通过 SseEmitter 返回）
     * @param message 消息
     * @param chatId 对话id
     * @return 响应
     */
    @GetMapping(value = "/love_app/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(1800000L);
        loveApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        DnManus dnManus = new DnManus(allTools, dashScopeChatModel);
        return dnManus.runStream(message);
    }
}
