package com.dn.dnaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/11 02:53
 **/
public class LoveAppContextualQueryAugmentationFactory {
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate promptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系客服 https://bing.com
                """);

        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(promptTemplate)
                .build();
    }
}
