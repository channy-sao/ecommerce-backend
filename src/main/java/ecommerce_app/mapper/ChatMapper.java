// ChatMapper.java
package ecommerce_app.mapper;

import ecommerce_app.dto.response.ChatMessageResponse;
import ecommerce_app.dto.response.ChatSessionResponse;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatMessageResponse toMessageResponse(ChatMessage msg) {
        User sender = msg.getSender();
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getFullName() : "System")
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .senderType(msg.getSenderType())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getCreatedAt())
                .build();
    }

    public ChatSessionResponse toSessionResponse(ChatSession session, long unreadCount) {
        User customer = session.getCustomer();
        User agent    = session.getAgent();
        return ChatSessionResponse.builder()
                .id(session.getId())
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .customerAvatar(customer.getAvatar())
                .agentId(agent != null ? agent.getId() : null)
                .agentName(agent != null ? agent.getFullName() : null)
                .status(session.getStatus())
                .unreadCount(unreadCount)
                .createdAt(session.getCreatedAt())
                .closedAt(session.getClosedAt())
                .build();
    }
}