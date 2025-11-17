import infrastructure.s3.service.S3ClientFactory;
import infrastructure.s3.service.S3SentService;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        String accessKey = "";
        String secretKey = "";
        String sessionToken = "";
        String bucketName = "raw-paymetrics";

        Path uploadFolder = Path.of("src/main/resources/upload");

        try(S3Client s3Client = S3ClientFactory.createClient(accessKey, secretKey, sessionToken, Region.US_EAST_1)){
            if(!Files.exists(uploadFolder)){
                Files.createDirectories(uploadFolder);
            }

            S3SentService sendService = new S3SentService(s3Client, bucketName);
            sendService.uploadAllCSV(uploadFolder);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
