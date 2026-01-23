package kz.event.s3;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import kz.event.s3.DTO.PresignPutResult;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final S3Properties props;

    private final Tika tika = new Tika();

    public PresignPutResult presignPut(String dir, String originalFilename) {
        String ext = safeExt(originalFilename);
        String key = normalizeDir(dir) + UUID.randomUUID() + ext;

        String contentType = detectContentTypeByName(originalFilename);
        if (contentType == null || contentType.isBlank()) contentType = "application/octet-stream";

        var putReq = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        var presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.getPresign().getPutSeconds()))
                .putObjectRequest(putReq)
                .build();

        String uploadUrl = presigner.presignPutObject(presignReq).url().toString();

        return PresignPutResult.builder()
                .key(key)
                .contentType(contentType)
                .uploadUrl(uploadUrl)
                .publicUrl(buildPublicUrl(key))
                .build();
    }

    private String detectContentTypeByName(String filename) {
        return tika.detect(filename == null ? "" : filename);
    }

    private String buildPublicUrl(String key) {
        if (props.getPublicBaseUrl() == null || props.getPublicBaseUrl().isBlank()) return "";
        String base = props.getPublicBaseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + key;
    }

    private String normalizeDir(String dir) {
        String d = (dir == null) ? "" : dir.trim();
        if (d.startsWith("/")) d = d.substring(1);
        if (!d.isEmpty() && !d.endsWith("/")) d += "/";
        return d;
    }

    private String safeExt(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        String ext = name.substring(i).toLowerCase();
        return ext.length() <= 10 ? ext : "";
    }

    public HeadObjectResponse head(String key) {
        return s3.headObject(HeadObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build());
    }
}
