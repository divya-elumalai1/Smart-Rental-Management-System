package com.smartrental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Entity representing a chat message exchanged with the AI assistant.
 *
 * <p>Each record stores the user's question and the AI's response. The chatbot
 * fetches real data from the database (rent due, pending amount, maintenance
 * status, payment history, lease expiry) and uses OpenAI GPT to format the answer.</p>
 */
@Entity
@Table(
    name = "chat_history",
    indexes = {
        @Index(name = "idx_chathistory_user_id", columnList = "user_id"),
        @Index(name = "idx_chathistory_created_at", columnList = "created_at")
    }
)
@SQLDelete(sql = "UPDATE chat_history SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
public class ChatHistory extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    /**
     * The user who sent the message.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_chathistory_user"))
    private User user;

    /**
     * The user's question or message to the AI assistant.
     */
    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    @Column(name = "message", nullable = false, length = 5000)
    private String message;

    /**
     * The AI assistant's response.
     */
    @Column(name = "response", length = 10000)
    private String response;

    /**
     * The OpenAI model used to generate the response.
     */
    @Column(name = "model_used", length = 50)
    private String modelUsed;

    /**
     * Number of tokens consumed for this interaction.
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /**
     * Conversation session ID to group related messages.
     */
    @Column(name = "conversation_id", columnDefinition = "uuid")
    private UUID conversationId;
}
