package kz.event.domain.user.DTO;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserProfileDto {
    private String username;
    private String fullName;
    private String bio;
    private String job;
    private String city;
    private LocalDate birthdate;
    private String sex;
}
