package kz.event.auth.DTO;

import lombok.Data;

@Data
public class RegisterDto {
    private String email;
    private String phone;
    private String password;
}
