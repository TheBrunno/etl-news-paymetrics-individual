package infrastructure.gemini.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class RequestGeminiAPI {
    public static String ask(String question){
        try(Client client = Client.builder().apiKey(System.getenv("GEMINI_KEY")).build()){
            GenerateContentResponse response =
                    client.models.generateContent(System.getenv("GEMINI_MODEL"), question, null);

            return response.text();
        }catch (Exception e){
            System.out.println(e);
        }
        return "";
    }
}
