package API;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class PrioritizedTaskHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        PrioritizedHandlerEndpoint endpoint = getEndpoint(exchange);
        if (endpoint == PrioritizedHandlerEndpoint.GET_PRIORITIZED) {
            handleGetPrioritized(exchange);
        } else {
            writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) {

    }

    private PrioritizedHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET")) {
            return PrioritizedHandlerEndpoint.GET_PRIORITIZED;
        }
        return PrioritizedHandlerEndpoint.UNKNOWN;
    }
}
