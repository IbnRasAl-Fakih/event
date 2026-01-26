package kz.event.domain.user.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserProfileDto {
    @NotBlank(message = "Username must not be empty")
    private String username;

    @NotBlank(message = "Full name must not be empty")
    private String fullName;

    private String bio;

    private String job;

    @NotBlank(message = "City must not be empty")
    private String city;

    @NotNull(message = "Birthdate must not be empty")
    private LocalDate birthdate;

    @NotNull(message = "Sex must not be empty")
    private String sex;

    private String filename;
}
