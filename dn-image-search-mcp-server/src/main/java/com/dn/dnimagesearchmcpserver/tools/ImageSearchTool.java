package com.dn.dnimagesearchmcpserver.tools;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * pixabay 搜索图片 API
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/12 22:32
 **/
@Service
public class ImageSearchTool {

//    private static final String API_KEY = "58006786-6f5b0c50d34e26ea90c6f3b79";

    private static final String API_URL = "https://pixabay.com/api/";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        String API_KEY = System.getenv("PIXABAY_API_KEY");
        if (StrUtil.isBlank(API_KEY)) {
            return "Api key not set";
        }

        try {
            return String.join(",", searchMediumImages(query, API_KEY));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query 搜索关键词
     * @return 图片URL列表
     */
    private List<String> searchMediumImages(String query, String apiKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", apiKey);
        params.put("q", query);
        params.put("lang", "zh");
        params.put("per_page", 20);
        params.put("safesearch", true);

        String response = HttpUtil.createGet(API_URL)
                .form(params)
                .execute()
                .body();

        return JSONUtil.parseObj(response)
                .getJSONArray("hits")
                .stream()
                .map(hit -> (JSONObject) hit)
                .map(hit -> hit.getStr("webformatURL"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
