package com.dn.dnaiagent.common;

import lombok.Data;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/1/13 23:48
 **/
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}


