package lambda;

import infrastructure.CSVtoJSON.CSVtoJSON;
import infrastructure.apinews.service.RequestNewsAPI;
import infrastructure.csv.service.ReadCSV;
import infrastructure.csv.service.WriteCSV;
import infrastructure.gemini.service.RequestGeminiAPI;
import infrastructure.s3.service.S3ClientFactory;
import infrastructure.s3.service.S3SentService;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void execute() {
        String promptNews = System.getenv("PROMPT_GEMINI");

        String bucketName = System.getenv("RAW_NAME");
        String bucketNameTrusted = System.getenv("TRUSTED_NAME");
        String csvFileName = "noticias_semana.csv";

        String apitubeKey = System.getenv("APITUBE_KEY");

        String[] excludeHeaders = {"ID", "Href", "Description", "Body", "Language", "Author", "Categories", "Topics", "Industries",
        "Entities", "Source", "Sentiment", "Summary", "Keywords", "Links", "Media", "Story", "IsDuplicate", "IsAccessibleForFree", "IsBreaking", "ReadTime",
        "SentencesCount", "ParagraphsCount", "WordsCount", "CharactersCount"};

        Path uploadFolder = Path.of("/tmp/upload");

        try(S3Client s3Client = S3ClientFactory.createClient(Region.US_EAST_1)){
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

            String responseCSV = requestHandlerCSV.call();

            Path filecsv = WriteCSV.write(uploadFolder, csvFileName, responseCSV);
            String result = ReadCSV.readSpecificColumn(uploadFolder, csvFileName, 3);

            String IAResponse = RequestGeminiAPI.ask(promptNews+result)
                    .replaceAll("```csv ", "")
                    .replaceAll("```", "")
                    .replaceAll("```csv", "")
                    .replaceAll("``` ", "");
            filecsv = WriteCSV.bindColumns(uploadFolder, csvFileName, IAResponse);

            S3SentService sendService = new S3SentService(s3Client, bucketName);
            sendService.uploadFile(filecsv);

            String csvPath = "/tmp/upload/noticias_semana.csv";
            String jsonPath = "/tmp/upload/noticias_semana.json";

            CSVtoJSON.csvToJson(csvPath, jsonPath, excludeHeaders);

            S3SentService sendServiceTrusted = new S3SentService(s3Client, bucketNameTrusted);
            sendServiceTrusted.uploadFile(Path.of(jsonPath));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
