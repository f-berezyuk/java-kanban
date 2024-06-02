package API;

import java.io.IOException;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EpicTaskHandler extends TaskHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        TaskHandlerEndpoint endpoint = getEndpoint(exchange);
        if(endpoint == TaskHandlerEndpoint.UNKNOWN) {
            writeResponse(exchange, "Такого эндпоинта не существует", 404);
        } else {
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
                case GET_SUBTASKS -> {
                }
            }
        }
    }

    @Override
    TaskHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        String[] pathParts = requestPath.split("/");

        if(pathParts.length == 4 && requestMethod.equals("GET") && Objects.equals(pathParts[3], "subtasks")){
            return TaskHandlerEndpoint.GET_SUBTASKS;
        } else {
            return super.getEndpoint(exchange);
        }
    }
}
