package com.dn.dnaiagent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/1/13 23:48
 **/
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}


