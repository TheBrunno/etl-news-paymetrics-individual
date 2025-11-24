package infrastructure.s3.service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientFactory {
    public static S3Client createClient(Region region) {
        return S3Client.builder()
                .region(region)
                .build();
    }
}