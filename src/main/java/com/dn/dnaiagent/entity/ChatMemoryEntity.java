package com.dn.dnaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/9/29 21:43
 **/
@Data
@TableName("chat_memory")
public class ChatMemoryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private String role;
    private String content;
    private String messageType;
    private String metadata; // JSON字符串
    private LocalDateTime createdAt;
}