package kz.event.auth.DTO;

import lombok.Data;

@Data
public class CodeCheckerDto {
    private String email;
    private String code;
}
