package api.handlers;

import java.io.IOException;

import api.HandlerUtilities;
import api.adapters.TaskListTypeToken;
import com.sun.net.httpserver.HttpExchange;
import tracker.TaskManager;

public class PrioritizedTaskHandler extends BaseHandler {
    public PrioritizedTaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            PrioritizedHandlerEndpoint endpoint = getEndpoint(exchange);
            if (endpoint == PrioritizedHandlerEndpoint.GET_PRIORITIZED) {
                handleGetPrioritized(exchange);
            } else {
                HandlerUtilities.writeEndpoint404Response(exchange);
            }
        } catch (IOException e) {
            HandlerUtilities.write500(exchange, e);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        HandlerUtilities.writeResponse(
                exchange,
                gson.toJson(manager.getPrioritizedTasks(), new TaskListTypeToken().getType()),
                200);
    }

    private PrioritizedHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET")) {
            return PrioritizedHandlerEndpoint.GET_PRIORITIZED;
        }
        return PrioritizedHandlerEndpoint.UNKNOWN;
    }
}
