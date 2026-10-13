package com.dn.dnimagesearchmcp;

import com.dn.dnimagesearchmcp.tools.ImageSearchTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DnImageSearchMcpApplicationTests {
    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void contextLoads() {
        String result = imageSearchTool.searchImage("computer");
        Assertions.assertNotNull(result);
    }
}
