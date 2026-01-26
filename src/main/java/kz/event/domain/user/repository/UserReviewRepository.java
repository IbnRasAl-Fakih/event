package kz.event.domain.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kz.event.domain.user.entity.UserReview;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, UUID> {
    
    List<UserReview> findByRecipientId(UUID recipientId);

    boolean existsByAuthorIdAndRecipientIdAndEventId(UUID authorId, UUID recipientId, UUID eventID);
}
