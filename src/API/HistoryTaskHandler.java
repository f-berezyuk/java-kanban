package API;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import static API.HistoryHandlerEndpoint.GET_HISTORY;
import static API.HistoryHandlerEndpoint.UNKNOWN;

public class HistoryTaskHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HistoryHandlerEndpoint endpoint = getEndpoint(exchange);
        if (endpoint == GET_HISTORY) {
            handleGetHistory(exchange);
        } else {
            writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    private void handleGetHistory(HttpExchange exchange) {
    }

    private HistoryHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET")) {
            return GET_HISTORY;
        }
        return UNKNOWN;
    }
}
