package infrastructure.CSVtoJSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class CSVtoJSON {

    public static void csvToJson(String csvPath, String jsonOutput, String[] excludeHeaders) {

        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {

            Set<String> excluidos = new HashSet<>();
            for (String h : excludeHeaders) excluidos.add(h.toLowerCase());

            CSVFormat format = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();

            CSVParser parser = format.parse(reader);

            String[] headers = parser.getHeaderNames().toArray(new String[0]);

            int penultimaPos = headers.length - 2;
            String penultimoHeader = headers[penultimaPos];

            JsonArray array = new JsonArray();

            for (CSVRecord linha : parser) {

                if (linha.get(penultimoHeader).equalsIgnoreCase("false")) {
                    continue;
                }

                JsonObject obj = new JsonObject();

                for (String header : headers) {

                    if (excluidos.contains(header.toLowerCase())) continue;

                    String value = linha.get(header);

                    obj.addProperty(header, value);
                }

                array.add(obj);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(jsonOutput))) {
                bw.write(gson.toJson(array));
            }

            System.out.println("Conversão concluída.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
