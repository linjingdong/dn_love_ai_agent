package com.dn.dnaiagent.chatmemory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dn.dnaiagent.entity.ChatMemoryEntity;
import com.dn.dnaiagent.service.ChatMemoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DbBasedChatMemory implements ChatMemory {

    @Resource
    private ChatMemoryService chatMemoryService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void add(String conversationId, Message message) {
        chatMemoryService.save(toEntity(conversationId, message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<ChatMemoryEntity> list = messages.stream()
                .map(msg -> toEntity(conversationId, msg))
                .toList();
        chatMemoryService.saveBatch(list);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (lastN <= 0) {
            return List.of();
        }
        List<ChatMemoryEntity> entities = chatMemoryService.list(
                new LambdaQueryWrapper<ChatMemoryEntity>()
                        .eq(ChatMemoryEntity::getConversationId, conversationId)
                        .orderByDesc(ChatMemoryEntity::getCreatedAt)
                        .last("LIMIT " + lastN)
        );
        Collections.reverse(entities);

        List<Message> messages = new ArrayList<>(entities.size());
        for (ChatMemoryEntity entity : entities) {
            try {
                messages.add(toMessage(entity));
            } catch (Exception e) {
                log.error("反序列化消息失败, id={}, type={}", entity.getId(), entity.getMessageType(), e);
            }
        }
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        chatMemoryService.remove(
                new LambdaQueryWrapper<ChatMemoryEntity>()
                        .eq(ChatMemoryEntity::getConversationId, conversationId)
        );
    }

    private ChatMemoryEntity toEntity(String conversationId, Message message) {
        ChatMemoryEntity entity = new ChatMemoryEntity();
        entity.setConversationId(conversationId);
        entity.setContent(message.getText());
        entity.setMessageType(String.valueOf(message.getMessageType()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRole("user");
        try {
            entity.setMetadata(objectMapper.writeValueAsString(message.getMetadata()));
        } catch (Exception e) {
            log.warn("序列化metadata失败, conversationId={}", conversationId, e);
            // ⚠️ metadata序列化失败不应阻断消息保存，降级为空JSON
            entity.setMetadata("{}");
        }
        return entity;
    }

    private Message toMessage(ChatMemoryEntity entity) {
        Map<String, Object> metadata = parseMetadata(entity.getMetadata());
        String content = entity.getContent() != null ? entity.getContent() : "";

        return switch (entity.getMessageType()) {
            case "USER" -> new UserMessage(content, List.of(), metadata);

            case "ASSISTANT" -> new AssistantMessage(content, metadata);

            case "SYSTEM" -> new SystemMessage(content);

            default -> {
                log.warn("未知消息类型: {}, 降级为UserMessage", entity.getMessageType());
                yield new UserMessage(content, List.of(), metadata);
            }
        };
    }

    private Map<String, Object> parseMetadata(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("反序列化metadata失败, json={}", json, e);
            return Map.of();
        }
    }
}