package API;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TaskHandler extends BaseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        TaskHandlerEndpoint endpoint = getEndpoint(exchange);
        switch (endpoint) {

            case GET_TASK -> {
            }
            case POST_TASK -> {
            }
            case GET_TASKS -> {
            }
            case GET_POSTS -> {
            }
            case DELETE_TASK -> {
            }
            case GET_SUBTASKS,
                    UNKNOWN -> writeResponse(exchange, "Такого эндпоинта не существует", 404);
        }
    }

    TaskHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("POST")) {
                return TaskHandlerEndpoint.POST_TASK;
            }
            if (requestMethod.equals("GET")) {
                return TaskHandlerEndpoint.GET_TASKS;
            }
        }
        if (pathParts.length == 3) {
            if (requestMethod.equals("GET")) {
                return TaskHandlerEndpoint.GET_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return TaskHandlerEndpoint.DELETE_TASK;
            }
        }
        return TaskHandlerEndpoint.UNKNOWN;
    }
}
