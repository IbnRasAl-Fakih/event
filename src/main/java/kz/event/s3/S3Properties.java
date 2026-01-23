package kz.event.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "s3")
public class S3Properties {
    private boolean enabled = true;

    private String bucket;
    private String region;

    private String accessKey;
    private String secretKey;

    private String endpoint;

    private boolean pathStyleAccess = true;

    private String publicBaseUrl;

    private Presign presign = new Presign();

    @Data
    public static class Presign {
        private int putSeconds = 300;
    }
}
