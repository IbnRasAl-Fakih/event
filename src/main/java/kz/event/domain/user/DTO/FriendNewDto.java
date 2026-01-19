package kz.event.domain.user.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendNewDto {
    @NotNull(message = "Friend id must not be empty")
    private UUID friendId;
}
