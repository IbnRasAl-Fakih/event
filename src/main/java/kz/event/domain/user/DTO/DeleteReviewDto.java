package kz.event.domain.user.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteReviewDto {
    @NotNull(message = "Id must not be empty")
    private UUID id;
}
