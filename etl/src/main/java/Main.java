import infrastructure.apinews.service.RequestNewsAPI;
import infrastructure.csv.service.WriteCSV;
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

        String apitubeKey = "";

        Path uploadFolder = Path.of("src/main/resources/upload");

        try(S3Client s3Client = S3ClientFactory.createClient(accessKey, secretKey, sessionToken, Region.US_EAST_1)){
            if(!Files.exists(uploadFolder)){
                Files.createDirectories(uploadFolder);
            }

            RequestNewsAPI requestHandlerCSV = new RequestNewsAPI(
                    "https://api.apitube.io/v1/news/everything"
                            + "?api_key=" + apitubeKey
                            + "&source.country.code=br"
                            + "&language.code=pt"
                            + "&title=economia,mercado,consumo,varejo,vendas"
                            + "&category.id=medtop:04000000,medtop:20000247,medtop:20001164"
                            + "&ignore.category.id=medtop:10000000,medtop:20000178,medtop:20001128"
                            + "&ignore.title=sexual,cinema,assaltado,sanitária,linux"
                            + "&sort_by=relevance"
                            + "&ignore.industry.id=1047,492,116"
                            + "&per_page=10"
                            + "&page=1"
                            + "&export=csv"
            );

            RequestNewsAPI requestHandlerJSON = new RequestNewsAPI(
                    "https://api.apitube.io/v1/news/everything"
                            + "?api_key=" + apitubeKey
                            + "&source.country.code=br"
                            + "&title=economia,mercado,consumo,varejo,vendas"
                            + "&language.code=pt"
                            + "&category.id=medtop:04000000,medtop:20000247,medtop:20001164"
                            + "&ignore.category.id=medtop:10000000,medtop:20000178,medtop:20001128"
                            + "&ignore.title=sexual,cinema,assaltado,sanitária,linux"
                            + "&sort_by=relevance"
                            + "&ignore.industry.id=1047,492,116"
                            + "&per_page=10"
                            + "&page=1"
                            + "&export=json"
            );

            String responseCSV = requestHandlerCSV.call();
            String responseJSON = requestHandlerJSON.call();

            Path filecsv = WriteCSV.write(uploadFolder, "noticias_semana.csv", responseCSV);
            Path filejson = WriteCSV.write(uploadFolder, "noticias_semana.json", responseJSON);

            S3SentService sendService = new S3SentService(s3Client, bucketName);
            sendService.uploadFile(filecsv);
            sendService.uploadFile(filejson);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
