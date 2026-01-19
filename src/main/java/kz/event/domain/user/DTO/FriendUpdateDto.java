package kz.event.domain.user.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendUpdateDto {

    @NotNull(message = "Id must not be empty")
    private UUID id;

    @NotNull(message = "Status must not be empty")
    private String status;
}
