package kz.event.s3.DTO;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PresignPutRequest {
    @NotBlank(message = "Dir name must not be empty")
    private String dir;
    
    @NotBlank(message = "File name must not be empty")
    private String filename;
}
