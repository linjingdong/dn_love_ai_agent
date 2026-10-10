package com.dn.dnaiagent.rag;

import com.dn.dnaiagent.exception.BusinessException;
import com.dn.dnaiagent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/30 21:36
 **/

@Component
@Slf4j
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resolver;

    LoveAppDocumentLoader(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * 加载多篇 Markdown 文件
     * @return
     */
    public List<Document> loadMarkDowns() {
        List<Document> documents = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String status = null;
                if (filename != null) {
                    status = filename.substring(filename.length() - 6, filename.length() - 4);
                }
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", Objects.requireNonNull(filename))
                        .withAdditionalMetadata("status", status)
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.read());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to load markdown documents");
        }
        return documents;
    }
}
