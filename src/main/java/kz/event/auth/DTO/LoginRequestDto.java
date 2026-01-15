package kz.event.auth.DTO;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
