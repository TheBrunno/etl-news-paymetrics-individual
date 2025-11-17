import infrastructure.apinews.service.RequestNewsAPI;
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
        String bucketName = "";

        String apitubeKey = "";

        Path uploadFolder = Path.of("src/main/resources/upload");

        try(S3Client s3Client = S3ClientFactory.createClient(accessKey, secretKey, sessionToken, Region.US_EAST_1)){
            if(!Files.exists(uploadFolder)){
                Files.createDirectories(uploadFolder);
            }

            RequestNewsAPI requestHandler = new RequestNewsAPI("https://api.apitube.io/v1/news/everything?api_key="+apitubeKey+"&source.country.code=br&language.code=pt&category.id=medtop:01000000&category.id=medtop:04000000&category.id=medtop:04001000&category.id=medtop:04004001&category.id=medtop:04004002&category.id=medtop:04009006&category.id=medtop:04006000&category.id=medtop:20000000&sort_by=published_at&page_size=5&export=csv");
            String response = requestHandler.call();

            S3SentService sendService = new S3SentService(s3Client, bucketName);
            sendService.uploadAllCSV(uploadFolder);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
