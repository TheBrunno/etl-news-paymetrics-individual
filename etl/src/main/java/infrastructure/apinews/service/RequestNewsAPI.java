package infrastructure.apinews.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestNewsAPI {
    private final String url;

    public RequestNewsAPI(String url) {
        this.url = url;
    }

    public String call() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
