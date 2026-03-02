package ecommerce_app.entity;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_message_session",  columnList = "session_id"),
    @Index(name = "idx_message_is_read",  columnList = "is_read")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    // Reuse your existing User entity!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true) // null for SYSTEM messages
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType; // CUSTOMER, AGENT, SYSTEM

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
}