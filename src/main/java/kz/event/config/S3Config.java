package kz.event.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kz.event.s3.S3Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties props) {
        var creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );

        var s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(props.isPathStyleAccess())
                .build();

        return S3Client.builder()
                .credentialsProvider(creds)
                .region(Region.of(props.getRegion() == null ? "us-east-1" : props.getRegion()))
                .endpointOverride(URI.create(props.getEndpoint()))
                .httpClient(UrlConnectionHttpClient.create())
                .serviceConfiguration(s3cfg)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        var creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );
        var s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(props.isPathStyleAccess())
                .build();

        return S3Presigner.builder()
                .credentialsProvider(creds)
                .region(Region.of(props.getRegion() == null ? "us-east-1" : props.getRegion()))
                .endpointOverride(URI.create(props.getEndpoint()))
                .serviceConfiguration(s3cfg)
                .build();
    }
}
