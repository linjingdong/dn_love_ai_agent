package com.dn.dnaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/10 22:09
 **/
@Component
public class MyKeywordEnricher {
    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher metadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        return metadataEnricher.apply(documents);
    }
}
