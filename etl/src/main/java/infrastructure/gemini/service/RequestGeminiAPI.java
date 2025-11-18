package infrastructure.gemini.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class RequestGeminiAPI {
    public static String ask(String question){
        try(Client client = Client.builder().apiKey("").build()){
            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", question, null);

            return response.text();
        }catch (Exception e){
            System.out.println(e);
        }
        return "";
    }
}
