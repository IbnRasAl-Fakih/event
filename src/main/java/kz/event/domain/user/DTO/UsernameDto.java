package kz.event.domain.user.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsernameDto {
    @NotNull(message = "Username must not be empty")
    private String username;
}
