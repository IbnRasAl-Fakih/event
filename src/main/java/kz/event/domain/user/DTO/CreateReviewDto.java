package kz.event.domain.user.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewDto {
    @NotNull(message = "Recipient id must not be empty")
    private UUID recipientId;

    @NotNull(message = "Event id must not be empty")
    private UUID eventId;

    private String text;

    @NotNull(message = "Rating must not be empty")
    private Integer rating;
}
