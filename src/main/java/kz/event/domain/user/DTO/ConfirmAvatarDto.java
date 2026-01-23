package kz.event.domain.user.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmAvatarDto {
    @NotBlank(message = "Key must not be empty")
    private String key;
}
