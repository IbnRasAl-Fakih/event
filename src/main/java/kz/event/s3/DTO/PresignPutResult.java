package kz.event.s3.DTO;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PresignPutResult {
    String contentType;
    String uploadUrl;
}
