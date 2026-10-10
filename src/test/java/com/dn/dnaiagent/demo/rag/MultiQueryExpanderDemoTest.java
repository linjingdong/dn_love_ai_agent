package com.dn.dnaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/10 22:33
 **/
@SpringBootTest
class MultiQueryExpanderDemoTest {
    @Resource
    private MultiQueryExpanderDemo expander;

    @Test
    void expand() {
        List<Query> queries = expander.expand("谁是程序员鱼皮啊，哈哈哈哈");
        Assertions.assertNotNull(queries);
    }
}