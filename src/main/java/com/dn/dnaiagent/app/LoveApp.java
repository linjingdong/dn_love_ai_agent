package com.dn.dnaiagent.app;

import com.dn.dnaiagent.advisor.MyLoggerAdvisor;
import com.dn.dnaiagent.chatmemory.DbBasedChatMemory;
import com.dn.dnaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.dn.dnaiagent.rag.QueryRewriter;
import com.dn.dnaiagent.tool.ToolRegistration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/29 15:26
 **/
@Component
@Slf4j
public class LoveApp {
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问:单身状态询问社交圈拓展及追求心仪对象的困扰;" +
            "恋爱状态询问沟通、习惯差异引发的矛盾;已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    private final ChatClient chatClient;

    /**
     * 初始化 chatClient
     * @param dashScoreChatModel
     */
    public LoveApp(ChatModel dashScoreChatModel, DbBasedChatMemory chatMemory) {
        /*
        // 创建基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        ChatMemory chatMemory = new InMemoryChatMemory();
        **/
        chatClient = ChatClient.builder(dashScoreChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * ai的简单对话demo
     * @param message 用户消息
     * @param chatId 对话id
     * @return content
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content -> {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * AI 恋爱报告功能（实现结构化输出）
     * @param message 用户消息
     * @param chatId 对话id
     * @return 返回
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结构，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);

        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和 rag 进行对话
     * @param message 用户消息
     * @param chatId 对话id
     * @return 返回
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        // String rewriterQuery = queryRewriter.doQueryRewriter(message);
        String content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                // 默认 RAG 知识库检索
                 .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // RAG 知识库服务增强检索
                // .advisors(loveAppRagCloudAdvisor)
                // RAG pgVector知识库检索
                // .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 自定义 RAG 知识库增强检索 （文档过滤 + 上下文查询检索）
                // .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "已婚"))
                .call()
                .content();

        log.info("content with rag: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    /**
     * 给 AI 赋能：工具调用
     * @param message 用户消息
     * @param chatId 对话id
     * @return 返回
     */
    public String doChatWithTools(String message, String chatId) {
        String content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .content();

        log.info("content with tool: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 给 AI 赋能：工具调用
     * @param message 用户消息
     * @param chatId 对话id
     * @return 返回
     */
    public String doChatWithMcp(String message, String chatId) {
        String content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbackProvider.getToolCallbacks())
                .call()
                .content();

        log.info("content with mcp: {}", content);
        return content;
    }
}
