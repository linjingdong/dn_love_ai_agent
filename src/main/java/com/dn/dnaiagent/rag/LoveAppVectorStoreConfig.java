package com.dn.dnaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 恋爱大师向量数据库配置（初始化基于内存的向量数据库 bean）
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/30 21:58
 **/
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 1. 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkDowns();
        // 2. 自主切分
        List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(documents);

        vectorStore.doAdd(documents);
        return vectorStore;
    }
}
