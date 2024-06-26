package api.handlers;

import java.io.IOException;

import api.HandlerUtilities;
import api.adapters.TaskListTypeToken;
import com.sun.net.httpserver.HttpExchange;
import tracker.TaskManager;

import static api.handlers.HistoryHandlerEndpoint.GET_HISTORY;
import static api.handlers.HistoryHandlerEndpoint.UNKNOWN;

public class HistoryTaskHandler extends BaseHandler {
    public HistoryTaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            HistoryHandlerEndpoint endpoint = getEndpoint(exchange);
            if (endpoint == GET_HISTORY) {
                handleGetHistory(exchange);
            } else {
                HandlerUtilities.writeEndpoint404Response(exchange);
            }
        } catch (IOException e) {
            HandlerUtilities.write500(exchange, e);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        HandlerUtilities.writeResponse(exchange,
                gson.toJson(manager.getHistory(), new TaskListTypeToken().getType()),
                200);
    }

    private HistoryHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET")) {
            return GET_HISTORY;
        }
        return UNKNOWN;
    }
}
