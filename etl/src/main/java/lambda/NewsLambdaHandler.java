package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class NewsLambdaHandler implements RequestHandler<Void, String> {

    @Override
    public String handleRequest(Void input, Context context) {
        try {
            Main.execute();
            return "OK";
        } catch (Exception e) {
            context.getLogger().log("Erro: " + e.getMessage());
            return "Erro: " + e.getMessage();
        }
    }
}