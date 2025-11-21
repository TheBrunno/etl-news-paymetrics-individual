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
    public static void main(String[] args) {
        String promptNews = """
                # Atue como um verificador de notícias para o meu contexto.
                
                Irei te enviar uma lista de títulos de notícias, você vai precisar retornar um CSV em texto, no seguinte padrão:
                
                ```csv<uma quebra de linha>
                faz_sentido,explicacao<uma quebra de linha>
                true,"... (resumo rapido da notícia explicando como e porque pode afetar o e-commerce)"
                ```
                
                Sem espaços antes e depois das virgulas (commas) divisoras.
                
                Na explicação coloque palavras chave em: <strong>palavra-chave</strong>, não retorne com "\\n <quebra de linha>" nem antes nem depois do csv
                
                Tenho um e-commerce que pode vender qualquer coisa, preciso de notícias que podem impactar no meu negócio.
                
                # TITULO DAS NOTICIAS
                """;

        String accessKey = "";
        String secretKey = "";
        String sessionToken = "";
        String bucketName = "raw-paymetrics";
        String bucketNameTrusted = "trusted-paymetrics";
        String csvFileName = "noticias_semana.csv";

        String apitubeKey = "";

        String[] excludeHeaders = {"ID", "Href", "Description", "Body", "Language", "Author", "Categories", "Topics", "Industries",
        "Entities", "Source", "Sentiment", "Summary", "Keywords", "Links", "Media", "Story", "IsDuplicate", "IsAccessibleForFree", "IsBreaking", "ReadTime",
        "SentencesCount", "ParagraphsCount", "WordsCount", "CharactersCount"};

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
            CSVtoJSON.csvToJson("src/main/resources/upload/noticias_semana.csv", "src/main/resources/upload/noticias_semana.json", excludeHeaders);

            S3SentService sendServiceTrusted = new S3SentService(s3Client, bucketNameTrusted);
            sendServiceTrusted.uploadFile(Path.of("src/main/resources/upload/noticias_semana.json"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
