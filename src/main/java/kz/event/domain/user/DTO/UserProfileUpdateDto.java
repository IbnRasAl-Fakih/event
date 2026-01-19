package kz.event.domain.user.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    @NotBlank(message = "Full name must not be empty")
    private String fullName;

    private String bio;

    private String job;

    @NotBlank(message = "City must not be empty")
    private String city;

    @NotNull(message = "Birthdate must not be empty")
    private LocalDate birthdate;
}
