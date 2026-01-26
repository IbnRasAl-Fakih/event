package kz.event.domain.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_reviews")
@Getter
@Setter
@NoArgsConstructor
public class UserReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorId;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    private String text;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    public UserReview(UUID authorId, UUID recipientId, UUID eventId, Integer rating, String text) {
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.eventId = eventId;
        this.rating = rating;
        this.text = text;
    }
}
