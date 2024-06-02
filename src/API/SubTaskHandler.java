package API;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class SubTaskHandler extends TaskHandler {
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
                    UNKNOWN -> {
                writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        }
    }
}
