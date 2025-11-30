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
            Você vai atuar como um CLASSIFICADOR DE NOTÍCIAS focado em impacto para e-commerce.
            
            Eu vou te enviar uma lista de títulos de notícias e você DEVE retornar SOMENTE um CSV em texto puro, exatamente neste formato:
            
            faz_sentido,explicacao,sentimento,impacto
            true,"... (resumo rápido explicando COMO e POR QUE a notícia pode afetar o e-commerce, usando pelo menos 2 palavras em <strong>negrito</strong>)",POSITIVO,ALTO
            
            REGRAS OBRIGATÓRIAS:
            1. NÃO retorne nada além do CSV.
               - Sem textos adicionais
               - Sem JSON
               - Sem markdown
               - Sem comentários
               - Sem blocos de código
            2. A primeira linha DEVE ser sempre o cabeçalho:
               faz_sentido,explicacao,sentimento,impacto
            3. Para cada título, gere EXATAMENTE UMA LINHA no CSV.
            4. Valores possíveis:
               - sentimento: POSITIVO, NEGATIVO, NEUTRO
               - impacto: ALTO, MÉDIO, BAIXO
            5. O campo explicacao deve:
               - Ficar entre aspas
               - Não conter quebras de linha
               - Incluir palavras-chave entre <strong> </strong>
            6. Não coloque linhas em branco antes ou depois do CSV.
            7. Caso não consiga seguir o formato, retorne apenas: ERRO
            
            Contexto:
            Tenho um e-commerce que pode vender qualquer produto. Avalie notícias que possam impactar demanda, tráfego, vendas ou carga nos servidores.
            
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
