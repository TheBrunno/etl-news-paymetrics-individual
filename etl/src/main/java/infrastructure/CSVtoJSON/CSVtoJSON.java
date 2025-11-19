package infrastructure.CSVtoJSON;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;

import java.io.*;

public class CSVtoJSON {

    public static void csvToJson(String csvPath, String jsonOutput, String[] excludeHeaders) {
        try (
                CSVReader reader = new CSVReader(new FileReader(csvPath));
                BufferedWriter bw = new BufferedWriter(new FileWriter(jsonOutput));
        ) {

            String[] headers = reader.readNext();

            JsonArray jsonArray = new JsonArray();

            String[] linha;
            while ((linha = reader.readNext()) != null) {

                JsonObject obj = new JsonObject();

                for (int i = 0; i < headers.length; i++) {

                    String key = headers[i];
                    boolean exclude = false;

                    for (String exc : excludeHeaders) {
                        if (key.equalsIgnoreCase(exc)) {
                            exclude = true;
                            break;
                        }
                    }
                    if (exclude) continue;

                    String value = "";
                    if(i < linha.length){
                        value = linha[i];
                    }

                    obj.addProperty(key, value);
                }

                jsonArray.add(obj);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            bw.write(gson.toJson(jsonArray));

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
